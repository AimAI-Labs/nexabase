package io.github.aimailabs.nexabase.ai.service;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * 会话多轮历史上下文记忆管理器。
 * <p>结合 Redis 缓存与 MySQL 回源，实施基于 Token 预算的安全滑动窗口裁剪。
 */
public interface ConversationMemoryManager {

    /**
     * 获取指定会话用于 Prompt 注入的多轮历史消息。
     * <p>按时间先后顺序排列，已按 maxTokens 预算完成逆序裁剪。
     *
     * @param sessionId 会话 UUID
     * @param maxTokens 最大 Token 预算阈值
     * @return LangChain4j ChatMessage 列表
     */
    List<ChatMessage> getPromptHistory(String sessionId, int maxTokens);

    /**
     * 向会话记忆末尾追加一条新消息（写 Redis 并刷新 TTL）。
     *
     * @param sessionId 会话 UUID
     * @param role      角色 (user / assistant / system)
     * @param content   消息正文
     */
    void appendMessage(String sessionId, String role, String content);

    /**
     * 清除会话的 Redis 记忆缓存。
     *
     * @param sessionId 会话 UUID
     */
    void clearMemory(String sessionId);
}
