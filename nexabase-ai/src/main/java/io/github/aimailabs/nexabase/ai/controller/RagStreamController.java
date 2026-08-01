package io.github.aimailabs.nexabase.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * SSE 连接超时时间（毫秒）。
     * <p>大模型流式生成耗时较长，设置 5 分钟兜底，避免 Tomcat 主动断连。
     */
    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;

    private final RagRetrievalService retrievalService;
    private final StreamingRagAssistant streamingRagAssistant;
    private final ObjectMapper objectMapper;

    /**
     * SSE 流式 RAG 问答。
     * <p>事件流：token(逐字) → sources(引用) → done。
     * <p>基于 Spring MVC 原生 {@link SseEmitter} 实现（本服务为 Servlet 栈，
     * 不使用 WebFlux 的 {@code Flux<ServerSentEvent>}，避免无对应 HttpMessageConverter
     * 导致 {@code No acceptable representation}）。
     *
     * @param request 问答请求
     * @return SSE 事件流
     */
    @Operation(summary = "SSE 流式 RAG 问答", description = "逐 token 推送回答，末尾推送引用来源")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody RagChatRequest request) {
        String query = request.getQuery();
        Long kbId = request.getKbId();
        int topK = request.getTopK();
        long startTs = System.currentTimeMillis();
        log.info("[RAG-Stream] 收到流式问答请求：query=\"{}\", kbId={}, topK={}", query, kbId, topK);

        // 检索 + Prompt 构建在请求线程同步完成，失败直接抛出（走全局异常处理）
        List<FusedHit> hits;
        String userMessage;
        try {
            long retrieveStart = System.currentTimeMillis();
            hits = retrievalService.retrieve(query, kbId, topK);
            long retrieveCost = System.currentTimeMillis() - retrieveStart;
            log.info("[RAG-Stream] 检索阶段完成：命中条数={}, 耗时={}ms", hits.size(), retrieveCost);

            userMessage = RagPromptTemplate.buildUserMessage(query, hits);
            log.info("[RAG-Stream] Prompt 构建完成：上下文片段数={}, prompt长度={}字符",
                    hits.size(), userMessage.length());
        } catch (Exception e) {
            log.error("[RAG-Stream] 检索/Prompt 阶段失败：query=\"{}\", 耗时={}ms, 错误={}",
                    query, System.currentTimeMillis() - startTs, e.getMessage(), e);
            throw e;
        }

        // 创建 SSE emitter，交由 LangChain4j TokenStream 异步推送
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        long llmStartTs = System.currentTimeMillis();
        AtomicInteger tokenCount = new AtomicInteger(0);
        StringBuilder answerBuf = new StringBuilder();

        // 异常兜底： emitter 超时 / 客户端断连 时记录日志，避免堆栈污染
        emitter.onTimeout(() -> log.warn("[RAG-Stream] SSE 连接超时：query=\"{}\", 已推送token数={}",
                query, tokenCount.get()));
        emitter.onError(ex -> log.warn("[RAG-Stream] SSE 连接异常：query=\"{}\", 已推送token数={}, 错误={}",
                query, tokenCount.get(), ex.getMessage()));

        TokenStream tokenStream = streamingRagAssistant.chat(userMessage);
        tokenStream
                .onPartialResponse(token -> {
                    tokenCount.incrementAndGet();
                    answerBuf.append(token);
                    sendEvent(emitter, "token", token);
                })
                .onCompleteResponse(response -> {
                    long llmCost = System.currentTimeMillis() - llmStartTs;
                    long totalCost = System.currentTimeMillis() - startTs;
                    int answerLen = answerBuf.length();
                    Integer inputTokens = null;
                    Integer outputTokens = null;
                    if (response != null && response.tokenUsage() != null) {
                        inputTokens = response.tokenUsage().inputTokenCount();
                        outputTokens = response.tokenUsage().outputTokenCount();
                    }
                    log.info("[RAG-Stream] 大模型流式生成完成：推送token数={}, 回答长度={}字符, " +
                                    "输入token={}, 输出token={}, 模型耗时={}ms, 总耗时={}ms",
                            tokenCount.get(), answerLen, inputTokens, outputTokens, llmCost, totalCost);
                    try {
                        sendEvent(emitter, "sources", hits);
                        sendEvent(emitter, "done", "");
                        emitter.complete();
                        log.info("[RAG-Stream] 流式问答成功：query=\"{}\", 召回={}, 引用={}, 总耗时={}ms",
                                query, hits.size(), hits.size(), totalCost);
                    } catch (Exception e) {
                        log.error("[RAG-Stream] 推送 sources/done 事件失败：query=\"{}\", 错误={}",
                                query, e.getMessage(), e);
                        emitter.completeWithError(e);
                    }
                })
                .onError(err -> {
                    long llmCost = System.currentTimeMillis() - llmStartTs;
                    long totalCost = System.currentTimeMillis() - startTs;
                    log.error("[RAG-Stream] 大模型流式生成失败：query=\"{}\", prompt长度={}字符, " +
                                    "已推送token数={}, 模型耗时={}ms, 总耗时={}ms, 错误={}",
                            query, userMessage.length(), tokenCount.get(), llmCost, totalCost,
                            err.getMessage(), err);
                    emitter.completeWithError(err);
                })
                .start();

        return emitter;
    }

    /**
     * 向 SSE 客户端推送一个命名事件。
     * <p>{@link SseEmitter#send(SseEventBuilder)} 默认使用 Jackson 序列化 data，
     * 对于 String 类型会原样输出，对于对象会序列化为 JSON。
     *
     * @param emitter SSE emitter
     * @param name    事件名（token / sources / done）
     * @param data    事件数据
     */
    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data));
        } catch (IOException | IllegalStateException e) {
            // 客户端已断开或连接已关闭，仅记录 debug，避免污染日志
            log.debug("[RAG-Stream] SSE 推送失败（客户端可能已断连）：event={}, 错误={}",
                    name, e.getMessage());
        }
    }
}
