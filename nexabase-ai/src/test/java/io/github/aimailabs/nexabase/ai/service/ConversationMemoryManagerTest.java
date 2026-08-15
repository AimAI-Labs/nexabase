package io.github.aimailabs.nexabase.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import io.github.aimailabs.nexabase.ai.mapper.ChatMessageMapper;
import io.github.aimailabs.nexabase.ai.service.impl.ConversationMemoryManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationMemoryManagerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private ChatMessageMapper messageMapper;

    private ConversationMemoryManager memoryManager;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        memoryManager = new ConversationMemoryManagerImpl(redisTemplate, messageMapper);
    }

    @Test
    @DisplayName("Redis 缓存命中时直接返回缓存历史")
    void testGetHistory_RedisHit() {
        String sessionId = "sess_001";
        List<Object> cached = List.of(
                "{\"role\":\"user\",\"content\":\"什么是 Nacos？\"}",
                "{\"role\":\"assistant\",\"content\":\"Nacos 是服务发现与配置中心。\"}"
        );
        when(listOperations.range("ai:chat:memory:" + sessionId, 0, -1)).thenReturn(cached);

        List<ChatMessage> result = memoryManager.getPromptHistory(sessionId, 2000);
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof UserMessage);
        assertTrue(result.get(1) instanceof AiMessage);
    }

    @Test
    @DisplayName("Redis 未命中时从 MySQL 回源并写入 Redis")
    void testGetHistory_RedisMiss_FallbackMySQL() {
        String sessionId = "sess_002";
        when(listOperations.range("ai:chat:memory:" + sessionId, 0, -1)).thenReturn(Collections.emptyList());

        io.github.aimailabs.nexabase.ai.entity.ChatMessage msg1 = new io.github.aimailabs.nexabase.ai.entity.ChatMessage();
        msg1.setRole("user");
        msg1.setContent("问题1");
        msg1.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        io.github.aimailabs.nexabase.ai.entity.ChatMessage msg2 = new io.github.aimailabs.nexabase.ai.entity.ChatMessage();
        msg2.setRole("assistant");
        msg2.setContent("回答1");
        msg2.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(msg1, msg2));

        List<ChatMessage> result = memoryManager.getPromptHistory(sessionId, 2000);
        assertEquals(2, result.size());
        verify(redisTemplate).expire(eq("ai:chat:memory:" + sessionId), eq(7L), eq(TimeUnit.DAYS));
    }

    @Test
    @DisplayName("Token 预算超限时逆序截断最早历史")
    void testTokenBudgetTruncation() {
        String sessionId = "sess_003";
        // 构造较长历史
        List<Object> cached = new ArrayList<>();
        cached.add("{\"role\":\"user\",\"content\":\"很长的问题1...\".repeat(50)}");
        cached.add("{\"role\":\"assistant\",\"content\":\"很长的回答1...\".repeat(50)}");
        cached.add("{\"role\":\"user\",\"content\":\"最近的问题2\"}");
        cached.add("{\"role\":\"assistant\",\"content\":\"最近的回答2\"}");

        when(listOperations.range("ai:chat:memory:" + sessionId, 0, -1)).thenReturn(cached);

        // 仅允许很小的 token 预算（如 50 tokens）
        List<ChatMessage> result = memoryManager.getPromptHistory(sessionId, 50);
        // 最早的第1轮问答由于超预算应被截断，只保留第2轮
        assertEquals(2, result.size());
        assertEquals("最近的问题2", ((UserMessage) result.get(0)).singleText());
        assertEquals("最近的回答2", ((AiMessage) result.get(1)).text());
    }
}
