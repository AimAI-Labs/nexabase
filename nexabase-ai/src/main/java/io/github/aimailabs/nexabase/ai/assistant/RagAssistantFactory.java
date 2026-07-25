package io.github.aimailabs.nexabase.ai.assistant;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AiServices Assistant 工厂：构建同步与流式 RAG Assistant。
 */
@Configuration
public class RagAssistantFactory {

    @Bean
    public RagAssistant ragAssistant(QwenChatModel chatModel) {
        return AiServices.builder(RagAssistant.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public StreamingRagAssistant streamingRagAssistant(QwenStreamingChatModel streamingModel) {
        return AiServices.builder(StreamingRagAssistant.class)
                .streamingChatModel(streamingModel)
                .build();
    }
}
