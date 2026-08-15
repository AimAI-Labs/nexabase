package io.github.aimailabs.nexabase.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.ai.dto.ChatMessageDTO;
import io.github.aimailabs.nexabase.ai.dto.FeedbackRequest;
import io.github.aimailabs.nexabase.ai.dto.RagSource;
import io.github.aimailabs.nexabase.ai.entity.ChatMessage;
import io.github.aimailabs.nexabase.ai.mapper.ChatMessageMapper;
import io.github.aimailabs.nexabase.ai.service.ChatMessageService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * AI 消息管理与持久化服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Async
    @Override
    public void saveMessagePairAsync(String sessionId, Long userId, String userQuery, String assistantAnswer,
                                     String rewriteQuery, List<RagSource> citations,
                                     Integer inputTokens, Integer outputTokens,
                                     Long retrieveCostMs, Long llmCostMs, Long totalCostMs) {
        try {
            Long uid = userId != null ? userId : 0L;
            // 1. 保存 User 提问
            ChatMessage userMsg = new ChatMessage();
            userMsg.setMessageId("msg_req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            userMsg.setSessionId(sessionId);
            userMsg.setUserId(uid);
            userMsg.setRole("user");
            userMsg.setContent(userQuery);
            userMsg.setRewriteQuery(rewriteQuery);
            messageMapper.insert(userMsg);

            // 2. 保存 Assistant 回答
            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setMessageId("msg_res_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
            assistantMsg.setSessionId(sessionId);
            assistantMsg.setUserId(uid);
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(assistantAnswer);
            assistantMsg.setRewriteQuery(rewriteQuery);
            if (citations != null && !citations.isEmpty()) {
                assistantMsg.setCitations(objectMapper.writeValueAsString(citations));
            }
            assistantMsg.setInputTokens(inputTokens != null ? inputTokens : 0);
            assistantMsg.setOutputTokens(outputTokens != null ? outputTokens : 0);
            assistantMsg.setRetrieveCostMs(retrieveCostMs != null ? retrieveCostMs.intValue() : 0);
            assistantMsg.setLlmCostMs(llmCostMs != null ? llmCostMs.intValue() : 0);
            assistantMsg.setTotalCostMs(totalCostMs != null ? totalCostMs.intValue() : 0);
            assistantMsg.setFeedbackStatus(0);
            messageMapper.insert(assistantMsg);

            log.info("[ChatMessage] 异步持久化问答成功：sessionId={}, userMsgId={}, assistantMsgId={}, totalCost={}ms",
                    sessionId, userMsg.getMessageId(), assistantMsg.getMessageId(), totalCostMs);
        } catch (Exception e) {
            log.error("[ChatMessage] 异步保存消息记录失败：sessionId={}", sessionId, e);
        }
    }

    @Override
    public Page<ChatMessageDTO> listMessages(String sessionId, Long userId, int page, int size) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "会话ID不能为空");
        }

        Page<ChatMessage> messagePage = new Page<>(page, size);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedAt);

        Page<ChatMessage> resultPage = messageMapper.selectPage(messagePage, wrapper);

        Page<ChatMessageDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).toList());
        return dtoPage;
    }

    @Override
    public boolean updateFeedback(String messageId, FeedbackRequest request, Long userId) {
        if (messageId == null || messageId.isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "消息ID不能为空");
        }
        ChatMessage msg = messageMapper.selectOne(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getMessageId, messageId));
        if (msg == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "消息记录不存在");
        }

        msg.setFeedbackStatus(request.getFeedbackStatus());
        if (request.getFeedbackRemark() != null) {
            msg.setFeedbackRemark(request.getFeedbackRemark());
        }

        int updated = messageMapper.updateById(msg);
        log.info("[ChatMessage] 更新反馈成功：messageId={}, status={}", messageId, request.getFeedbackStatus());
        return updated > 0;
    }

    private ChatMessageDTO convertToDTO(ChatMessage entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        BeanUtils.copyProperties(entity, dto);
        if (entity.getCitations() != null && !entity.getCitations().isBlank()) {
            try {
                List<RagSource> list = objectMapper.readValue(entity.getCitations(), new TypeReference<>() {});
                dto.setCitations(list);
            } catch (JsonProcessingException e) {
                dto.setCitations(Collections.emptyList());
            }
        }
        return dto;
    }
}
