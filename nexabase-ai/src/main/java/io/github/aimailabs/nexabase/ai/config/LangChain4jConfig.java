package io.github.aimailabs.nexabase.ai.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j DashScope 模型配置。
 * <p>Embedding(text-embedding-v3, 1024 维) + Chat(qwen-plus) + StreamingChat(qwen-plus)。
 * <p>API Key 等敏感凭据放 Nacos {@code nexabase-ai-dev.yml}，本地仅占位。
 */
@Configuration
public class LangChain4jConfig {

    @Bean
    public QwenEmbeddingModel qwenEmbeddingModel(
            @Value("${langchain4j.dashscope.api-key}") String apiKey,
            @Value("${langchain4j.dashscope.embedding-model:text-embedding-v3}") String embeddingModel) {
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModel)
                .build();
    }

    @Bean
    public QwenChatModel qwenChatModel(
            @Value("${langchain4j.dashscope.api-key}") String apiKey,
            @Value("${langchain4j.dashscope.chat-model:qwen-plus}") String chatModel) {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModel)
                .build();
    }

    @Bean
    public QwenStreamingChatModel qwenStreamingChatModel(
            @Value("${langchain4j.dashscope.api-key}") String apiKey,
            @Value("${langchain4j.dashscope.chat-model:qwen-plus}") String chatModel) {
        return QwenStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(chatModel)
                .build();
    }
}
