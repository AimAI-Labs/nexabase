package io.github.aimailabs.nexabase.ai.assistant;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import io.github.aimailabs.nexabase.ai.prompt.RagPromptTemplate;

/**
 * RAG 流式问答 Assistant。
 */
public interface StreamingRagAssistant {

    @SystemMessage(RagPromptTemplate.SYSTEM)
    TokenStream chat(@UserMessage String userMessage);
}
