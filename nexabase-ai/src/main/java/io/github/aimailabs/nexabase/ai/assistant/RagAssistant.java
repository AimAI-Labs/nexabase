package io.github.aimailabs.nexabase.ai.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.github.aimailabs.nexabase.ai.prompt.RagPromptTemplate;

/**
 * RAG 同步问答 Assistant（由 AiServices 构建）。
 * <p>系统提示为编译期常量 {@link RagPromptTemplate#SYSTEM}，用户消息含检索上下文。
 */
public interface RagAssistant {

    @SystemMessage(RagPromptTemplate.SYSTEM)
    String chat(@UserMessage String userMessage);
}
