package io.github.aimailabs.nexabase.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.ai.dto.ChatSessionDTO;
import io.github.aimailabs.nexabase.ai.dto.CreateSessionRequest;
import io.github.aimailabs.nexabase.ai.dto.UpdateSessionRequest;
import io.github.aimailabs.nexabase.ai.entity.ChatMessage;
import io.github.aimailabs.nexabase.ai.entity.ChatSession;
import io.github.aimailabs.nexabase.ai.mapper.ChatMessageMapper;
import io.github.aimailabs.nexabase.ai.mapper.ChatSessionMapper;
import io.github.aimailabs.nexabase.ai.service.ChatSessionService;
import io.github.aimailabs.nexabase.ai.service.ConversationMemoryManager;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * AI 会话管理服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final ConversationMemoryManager memoryManager;

    @Override
    public ChatSessionDTO createSession(CreateSessionRequest request, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        ChatSession session = new ChatSession();
        session.setSessionId("sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        session.setUserId(userId);
        session.setTitle(request != null && request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle() : "新对话");
        session.setKbId(request != null ? request.getKbId() : null);
        session.setModelName(request != null && request.getModelName() != null ? request.getModelName() : "qwen-plus");
        session.setSystemPrompt(request != null ? request.getSystemPrompt() : null);
        session.setTemperature(request != null && request.getTemperature() != null
                ? request.getTemperature() : BigDecimal.valueOf(0.70));
        session.setIsPinned(0);

        sessionMapper.insert(session);
        log.info("[ChatSession] 创建会话成功：sessionId={}, userId={}, title=\"{}\"",
                session.getSessionId(), userId, session.getTitle());

        return convertToDTO(session);
    }

    @Override
    public Page<ChatSessionDTO> listSessions(Long userId, Long kbId, int page, int size) {
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录");
        }

        Page<ChatSession> sessionPage = new Page<>(page, size);
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(kbId != null, ChatSession::getKbId, kbId)
                .orderByDesc(ChatSession::getIsPinned)
                .orderByDesc(ChatSession::getUpdatedAt);

        Page<ChatSession> resultPage = sessionMapper.selectPage(sessionPage, wrapper);

        Page<ChatSessionDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).toList());
        return dtoPage;
    }

    @Override
    public boolean updateSession(String sessionId, UpdateSessionRequest request, Long userId) {
        ChatSession session = getSessionEntity(sessionId, userId);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            session.setTitle(request.getTitle());
        }
        if (request.getIsPinned() != null) {
            session.setIsPinned(request.getIsPinned());
        }
        if (request.getKbId() != null) {
            session.setKbId(request.getKbId());
        }
        if (request.getModelName() != null && !request.getModelName().isBlank()) {
            session.setModelName(request.getModelName());
        }
        if (request.getSystemPrompt() != null) {
            session.setSystemPrompt(request.getSystemPrompt());
        }
        if (request.getTemperature() != null) {
            session.setTemperature(request.getTemperature());
        }

        int updated = sessionMapper.updateById(session);
        log.info("[ChatSession] 更新会话成功：sessionId={}, userId={}", sessionId, userId);
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(String sessionId, Long userId) {
        ChatSession session = getSessionEntity(sessionId, userId);

        // 逻辑删除会话
        sessionMapper.deleteById(session.getId());

        // 逻辑删除关联的消息
        messageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId));

        // 清理 Redis 记忆
        memoryManager.clearMemory(sessionId);

        log.info("[ChatSession] 删除会话及历史消息成功：sessionId={}, userId={}", sessionId, userId);
        return true;
    }

    @Override
    public ChatSession getOrCreateSession(String sessionId, Long userId, Long defaultKbId) {
        if (sessionId != null && !sessionId.isBlank()) {
            ChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                    .eq(ChatSession::getSessionId, sessionId)
                    .eq(userId != null, ChatSession::getUserId, userId));
            if (session != null) {
                return session;
            }
        }

        // 自动创建新会话
        ChatSession newSession = new ChatSession();
        newSession.setSessionId(sessionId != null && !sessionId.isBlank()
                ? sessionId : "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        newSession.setUserId(userId != null ? userId : 0L);
        newSession.setTitle("新对话");
        newSession.setKbId(defaultKbId);
        newSession.setModelName("qwen-plus");
        newSession.setTemperature(BigDecimal.valueOf(0.70));
        newSession.setIsPinned(0);

        sessionMapper.insert(newSession);
        log.info("[ChatSession] 自动创建新会话：sessionId={}, userId={}, kbId={}",
                newSession.getSessionId(), userId, defaultKbId);
        return newSession;
    }

    private ChatSession getSessionEntity(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会话ID不能为空");
        }
        ChatSession session = sessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));
        if (session == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "会话不存在或已被删除");
        }
        if (userId != null && !userId.equals(session.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问此会话");
        }
        return session;
    }

    private ChatSessionDTO convertToDTO(ChatSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        BeanUtils.copyProperties(session, dto);
        return dto;
    }
}
