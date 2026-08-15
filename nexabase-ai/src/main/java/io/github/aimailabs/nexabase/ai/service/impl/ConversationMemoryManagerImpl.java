package io.github.aimailabs.nexabase.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.github.aimailabs.nexabase.ai.mapper.ChatMessageMapper;
import io.github.aimailabs.nexabase.ai.service.ConversationMemoryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 会话多轮历史上下文记忆管理器实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryManagerImpl implements ConversationMemoryManager {

    private static final String REDIS_KEY_PREFIX = "ai:chat:memory:";
    private static final long REDIS_TTL_DAYS = 7;
    private static final int MAX_CACHE_MESSAGES = 20;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageMapper messageMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ChatMessage> getPromptHistory(String sessionId, int maxTokens) {
        if (sessionId == null || sessionId.isBlank()) {
            return Collections.emptyList();
        }

        List<RawMemoryMessage> rawMessages = loadFromCacheOrDb(sessionId);
        if (rawMessages.isEmpty()) {
            return Collections.emptyList();
        }

        // 逆序进行 Token 预算截断 (优先保留最新轮次)
        List<RawMemoryMessage> selected = new ArrayList<>();
        int currentEstimatedTokens = 0;
        for (int i = rawMessages.size() - 1; i >= 0; i--) {
            RawMemoryMessage m = rawMessages.get(i);
            int estimatedTokens = estimateTokens(m.content());
            if (currentEstimatedTokens + estimatedTokens > maxTokens && !selected.isEmpty()) {
                break;
            }
            selected.add(0, m);
            currentEstimatedTokens += estimatedTokens;
        }

        // 转换为 LangChain4j ChatMessage 对象
        List<ChatMessage> result = new ArrayList<>(selected.size());
        for (RawMemoryMessage m : selected) {
            if ("user".equalsIgnoreCase(m.role())) {
                result.add(UserMessage.from(m.content()));
            } else if ("assistant".equalsIgnoreCase(m.role())) {
                result.add(AiMessage.from(m.content()));
            } else if ("system".equalsIgnoreCase(m.role())) {
                result.add(SystemMessage.from(m.content()));
            }
        }
        return result;
    }

    @Override
    public void appendMessage(String sessionId, String role, String content) {
        if (sessionId == null || sessionId.isBlank() || role == null || content == null) {
            return;
        }
        String key = REDIS_KEY_PREFIX + sessionId;
        try {
            String json = objectMapper.writeValueAsString(Map.of("role", role, "content", content));
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.opsForList().trim(key, -MAX_CACHE_MESSAGES, -1);
            redisTemplate.expire(key, REDIS_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("向 Redis 追加消息记忆失败：sessionId={}", sessionId, e);
        }
    }

    @Override
    public void clearMemory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        try {
            redisTemplate.delete(REDIS_KEY_PREFIX + sessionId);
        } catch (Exception e) {
            log.warn("清理 Redis 记忆失败：sessionId={}", sessionId, e);
        }
    }

    private List<RawMemoryMessage> loadFromCacheOrDb(String sessionId) {
        String key = REDIS_KEY_PREFIX + sessionId;
        try {
            List<Object> cached = redisTemplate.opsForList().range(key, 0, -1);
            if (cached != null && !cached.isEmpty()) {
                List<RawMemoryMessage> list = new ArrayList<>();
                for (Object item : cached) {
                    RawMemoryMessage parsed = parseJson(item == null ? "" : item.toString());
                    if (parsed != null) {
                        list.add(parsed);
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            }
        } catch (Exception e) {
            log.warn("从 Redis 读取历史消息异常，回源 MySQL：sessionId={}", sessionId, e);
        }

        // 回源 MySQL 查询最近 20 条
        List<io.github.aimailabs.nexabase.ai.entity.ChatMessage> dbList = messageMapper.selectList(
                new LambdaQueryWrapper<io.github.aimailabs.nexabase.ai.entity.ChatMessage>()
                        .eq(io.github.aimailabs.nexabase.ai.entity.ChatMessage::getSessionId, sessionId)
                        .orderByAsc(io.github.aimailabs.nexabase.ai.entity.ChatMessage::getCreatedAt)
                        .last("LIMIT " + MAX_CACHE_MESSAGES)
        );

        if (dbList == null || dbList.isEmpty()) {
            return Collections.emptyList();
        }

        List<RawMemoryMessage> list = new ArrayList<>();
        try {
            redisTemplate.delete(key);
            for (io.github.aimailabs.nexabase.ai.entity.ChatMessage msg : dbList) {
                RawMemoryMessage raw = new RawMemoryMessage(msg.getRole(), msg.getContent());
                list.add(raw);
                String json = objectMapper.writeValueAsString(Map.of("role", msg.getRole(), "content", msg.getContent()));
                redisTemplate.opsForList().rightPush(key, json);
            }
            redisTemplate.expire(key, REDIS_TTL_DAYS, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("回写 Redis 记忆失败：sessionId={}", sessionId, e);
        }
        return list;
    }

    private RawMemoryMessage parseJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            String role = node.path("role").asText("user");
            String content = node.path("content").asText("");
            return new RawMemoryMessage(role, content);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // 粗略估算：中文约 1.2 字符/Token，英文约 4 字符/Token，取 1.5 字符 ~ 1 Token
        return Math.max(1, (int) (text.length() / 1.5));
    }

    private record RawMemoryMessage(String role, String content) {}
}
