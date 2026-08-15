package io.github.aimailabs.nexabase.ai.service;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.Response;
import io.github.aimailabs.nexabase.ai.service.impl.QueryRewriteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryRewriteServiceTest {

    @Mock
    private QwenChatModel chatModel;

    private QueryRewriteService rewriteService;

    @BeforeEach
    void setUp() {
        rewriteService = new QueryRewriteServiceImpl(chatModel);
    }

    @Test
    @DisplayName("首轮问答无历史时直接返回原 Query，不调用 LLM")
    void testRewrite_NoHistory_ReturnsOriginalQuery() {
        String query = "什么是 Spring Boot？";
        String result = rewriteService.rewrite(query, Collections.emptyList());
        assertEquals(query, result);
        verifyNoInteractions(chatModel);
    }

    @Test
    @DisplayName("多轮追问时调用 LLM 成功将代词重写为独立语句")
    void testRewrite_WithHistory_CallsLlmAndRewrites() {
        List<ChatMessage> history = List.of(
                UserMessage.from("什么是 Nacos 3.0？"),
                AiMessage.from("Nacos 3.0 是新一代服务发现与配置中心。")
        );
        String query = "它支持哪些部署模式？";
        String expectedRewritten = "Nacos 3.0 支持哪些部署模式？";

        dev.langchain4j.model.chat.response.ChatResponse response = dev.langchain4j.model.chat.response.ChatResponse.builder()
                .aiMessage(AiMessage.from(expectedRewritten))
                .build();
        when(chatModel.chat(anyList())).thenReturn(response);

        String result = rewriteService.rewrite(query, history);
        assertEquals(expectedRewritten, result);
    }

    @Test
    @DisplayName("LLM 调用异常时优雅降级返回原 Query")
    void testRewrite_LlmException_GracefulFallback() {
        List<ChatMessage> history = List.of(
                UserMessage.from("什么是 Redis？"),
                AiMessage.from("Redis 是高性能缓存。")
        );
        String query = "它怎么持久化？";

        when(chatModel.chat(anyList())).thenThrow(new RuntimeException("LLM Timeout"));

        String result = rewriteService.rewrite(query, history);
        assertEquals(query, result);
    }
}
