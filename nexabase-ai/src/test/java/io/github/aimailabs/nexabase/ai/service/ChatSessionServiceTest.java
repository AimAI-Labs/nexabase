package io.github.aimailabs.nexabase.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.ai.dto.ChatSessionDTO;
import io.github.aimailabs.nexabase.ai.dto.CreateSessionRequest;
import io.github.aimailabs.nexabase.ai.dto.UpdateSessionRequest;
import io.github.aimailabs.nexabase.ai.entity.ChatSession;
import io.github.aimailabs.nexabase.ai.mapper.ChatMessageMapper;
import io.github.aimailabs.nexabase.ai.mapper.ChatSessionMapper;
import io.github.aimailabs.nexabase.ai.service.impl.ChatSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionMapper sessionMapper;

    @Mock
    private ChatMessageMapper messageMapper;

    @Mock
    private ConversationMemoryManager memoryManager;

    private ChatSessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new ChatSessionServiceImpl(sessionMapper, messageMapper, memoryManager);
    }

    @Test
    @DisplayName("创建会话成功")
    void testCreateSession_Success() {
        CreateSessionRequest req = new CreateSessionRequest();
        req.setTitle("测试会话");
        req.setKbId(1001L);
        req.setTemperature(BigDecimal.valueOf(0.5));

        ChatSessionDTO dto = sessionService.createSession(req, 10001L);

        assertNotNull(dto.getSessionId());
        assertEquals("测试会话", dto.getTitle());
        assertEquals(1001L, dto.getKbId());
        verify(sessionMapper).insert(any(ChatSession.class));
    }

    @Test
    @DisplayName("更新会话配置成功")
    void testUpdateSession_Success() {
        String sessionId = "sess_test123";
        ChatSession existing = new ChatSession();
        existing.setId(1L);
        existing.setSessionId(sessionId);
        existing.setUserId(10001L);
        existing.setTitle("旧标题");

        when(sessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(sessionMapper.updateById(any(ChatSession.class))).thenReturn(1);

        UpdateSessionRequest req = new UpdateSessionRequest();
        req.setTitle("新标题");
        req.setIsPinned(1);

        boolean updated = sessionService.updateSession(sessionId, req, 10001L);
        assertTrue(updated);
        assertEquals("新标题", existing.getTitle());
        assertEquals(1, existing.getIsPinned());
    }

    @Test
    @DisplayName("删除会话并清除 Redis 记忆")
    void testDeleteSession_Success() {
        String sessionId = "sess_del123";
        ChatSession existing = new ChatSession();
        existing.setId(1L);
        existing.setSessionId(sessionId);
        existing.setUserId(10001L);

        when(sessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(sessionMapper.deleteById(1L)).thenReturn(1);

        boolean deleted = sessionService.deleteSession(sessionId, 10001L);
        assertTrue(deleted);
        verify(memoryManager).clearMemory(sessionId);
    }
}
