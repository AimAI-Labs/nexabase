package io.github.aimailabs.nexabase.ai.service;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

/**
 * 意图改写与独立问题生成服务。
 */
public interface QueryRewriteService {

    /**
     * 将包含多轮上下文的历史与当前提问改写为独立的 Standalone Query。
     *
     * @param query   当前轮提问
     * @param history 历史问答列表
     * @return 独立检索 Query (若无历史则原样返回)
     */
    String rewrite(String query, List<ChatMessage> history);
}
