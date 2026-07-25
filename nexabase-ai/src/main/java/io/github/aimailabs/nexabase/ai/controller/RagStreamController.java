package io.github.aimailabs.nexabase.ai.controller;

import dev.langchain4j.service.TokenStream;
import io.github.aimailabs.nexabase.ai.assistant.StreamingRagAssistant;
import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.dto.RagChatRequest;
import io.github.aimailabs.nexabase.ai.prompt.RagPromptTemplate;
import io.github.aimailabs.nexabase.ai.service.RagRetrievalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 问答 / SSE 流式问答接口
 *
 * @module nexabase-ai
 */
@Slf4j
@Tag(name = "RAG 问答", description = "BM25+向量多路召回 RRF 融合 + 大模型流式生成")
@RestController
@RequestMapping("/api/v1/ai/rag")
@RequiredArgsConstructor
public class RagStreamController {

    private final RagRetrievalService retrievalService;
    private final StreamingRagAssistant streamingRagAssistant;

    /**
     * SSE 流式 RAG 问答。
     * <p>事件流：token(逐字) → sources(引用) → done。
     *
     * @param request 问答请求
     * @return SSE 事件流
     */
    @Operation(summary = "SSE 流式 RAG 问答", description = "逐 token 推送回答，末尾推送引用来源")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> chatStream(@RequestBody RagChatRequest request) {
        List<FusedHit> hits = retrievalService.retrieve(request.getQuery(), request.getKbId(), request.getTopK());
        String userMessage = RagPromptTemplate.buildUserMessage(request.getQuery(), hits);

        return Flux.create(sink -> {
            TokenStream tokenStream = streamingRagAssistant.chat(userMessage);
            tokenStream
                    .onPartialResponse(token -> sink.next(ServerSentEvent.builder((Object) token).event("token").build()))
                    .onCompleteResponse(response -> {
                        sink.next(ServerSentEvent.builder((Object) hits).event("sources").build());
                        sink.next(ServerSentEvent.builder((Object) "").event("done").build());
                        sink.complete();
                    })
                    .onError(sink::error)
                    .start();
        });
    }
}
