package io.github.aimailabs.nexabase.ai.service.impl;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.dto.RagSource;
import io.github.aimailabs.nexabase.ai.dto.StreamChatRequest;
import io.github.aimailabs.nexabase.ai.dto.sse.CitationEventPayload;
import io.github.aimailabs.nexabase.ai.dto.sse.MessageDeltaPayload;
import io.github.aimailabs.nexabase.ai.dto.sse.MessageEndPayload;
import io.github.aimailabs.nexabase.ai.dto.sse.ThoughtEventPayload;
import io.github.aimailabs.nexabase.ai.entity.ChatSession;
import io.github.aimailabs.nexabase.ai.prompt.MultiTurnRagPromptTemplate;
import io.github.aimailabs.nexabase.ai.service.ChatMessageService;
import io.github.aimailabs.nexabase.ai.service.ChatSessionService;
import io.github.aimailabs.nexabase.ai.service.ConversationMemoryManager;
import io.github.aimailabs.nexabase.ai.service.QueryRewriteService;
import io.github.aimailabs.nexabase.ai.service.RagRetrievalService;
import io.github.aimailabs.nexabase.ai.service.StreamChatService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 多轮流式对话服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamChatServiceImpl implements StreamChatService {

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L;
    private static final int MAX_PROMPT_HISTORY_TOKENS = 2500;

    private final ChatSessionService sessionService;
    private final ChatMessageService messageService;
    private final ConversationMemoryManager memoryManager;
    private final QueryRewriteService rewriteService;
    private final RagRetrievalService retrievalService;
    private final QwenStreamingChatModel streamingChatModel;

    @Override
    public SseEmitter chatStream(StreamChatRequest request, Long userId) {
        if (request == null || request.getQuery() == null || request.getQuery().isBlank()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "提问内容不能为空");
        }

        long startTs = System.currentTimeMillis();
        String query = request.getQuery().trim();
        int topK = request.getTopK() != null && request.getTopK() > 0 ? request.getTopK() : 5;

        // 1. 获取或创建会话
        ChatSession session = sessionService.getOrCreateSession(request.getSessionId(), userId, request.getKbId());
        String sessionId = session.getSessionId();
        Long effectiveKbId = request.getKbId() != null ? request.getKbId() : session.getKbId();

        log.info("[StreamChat] 收到流式提问请求：sessionId={}, userId={}, query=\"{}\", kbId={}, topK={}",
                sessionId, userId, query, effectiveKbId, topK);

        // 2. 获取多轮历史上下文 (Token 预算裁剪)
        List<ChatMessage> history = memoryManager.getPromptHistory(sessionId, MAX_PROMPT_HISTORY_TOKENS);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        // 异常与断连监听
        emitter.onTimeout(() -> {
            log.warn("[StreamChat] SSE 连接超时：sessionId={}", sessionId);
            isCompleted.set(true);
        });
        emitter.onError(ex -> {
            log.warn("[StreamChat] SSE 客户端异常或断连：sessionId={}, 错误={}", sessionId, ex.getMessage());
            isCompleted.set(true);
        });

        // 异步或在请求线程中启动流式调度
        try {
            // 3. 意图改写 (多轮场景下触发)
            String rewriteQuery = query;
            if (history != null && !history.isEmpty()) {
                rewriteQuery = rewriteService.rewrite(query, history);
                if (!query.equals(rewriteQuery)) {
                    sendEvent(emitter, "thought", new ThoughtEventPayload("rewrite",
                            "结合历史上下文，正在将问题重写为独立查询：'" + rewriteQuery + "'"));
                }
            }

            // 4. 多路混合检索与 RRF 融合
            long retrieveStartTs = System.currentTimeMillis();
            List<FusedHit> hits = retrievalService.retrieve(rewriteQuery, effectiveKbId, topK);
            long retrieveCostMs = System.currentTimeMillis() - retrieveStartTs;

            // 5. 下发 citation 事件
            List<RagSource> sources = hits.stream().map(h -> {
                RagSource s = new RagSource();
                s.setDocId(h.getDocId());
                s.setTitle(h.getTitle());
                s.setSnippet(h.getSnippet());
                s.setScore(h.getScore());
                s.setSource(h.getSource());
                return s;
            }).toList();
            sendEvent(emitter, "citation", new CitationEventPayload(sessionId, sources));

            // 6. 构建完整 Prompt 消息序列 (System Prompt + 多轮历史 + User Query)
            String systemText = MultiTurnRagPromptTemplate.buildSystemPrompt(session.getSystemPrompt(), hits);
            List<ChatMessage> fullMessages = new ArrayList<>();
            fullMessages.add(SystemMessage.from(systemText));
            if (history != null && !history.isEmpty()) {
                fullMessages.addAll(history);
            }
            fullMessages.add(UserMessage.from(query));

            // 7. 启动大模型流式生成
            String assistantMessageId = "msg_res_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            long llmStartTs = System.currentTimeMillis();
            AtomicInteger tokenIndex = new AtomicInteger(0);
            StringBuilder answerBuffer = new StringBuilder();
            String finalRewriteQuery = rewriteQuery;

            streamingChatModel.chat(fullMessages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    if (isCompleted.get() || token == null) {
                        return;
                    }
                    int idx = tokenIndex.getAndIncrement();
                    answerBuffer.append(token);
                    sendEvent(emitter, "message", MessageDeltaPayload.of(assistantMessageId, "assistant", token, idx));
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    if (isCompleted.get()) {
                        return;
                    }
                    long llmCostMs = System.currentTimeMillis() - llmStartTs;
                    long totalCostMs = System.currentTimeMillis() - startTs;
                    String fullAnswer = answerBuffer.toString();

                    int inTokens = 0;
                    int outTokens = 0;
                    if (response != null && response.tokenUsage() != null) {
                        inTokens = response.tokenUsage().inputTokenCount() != null ? response.tokenUsage().inputTokenCount() : 0;
                        outTokens = response.tokenUsage().outputTokenCount() != null ? response.tokenUsage().outputTokenCount() : 0;
                    }

                    // 推送 message_end 事件
                    MessageEndPayload endPayload = new MessageEndPayload(
                            sessionId,
                            "msg_req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                            assistantMessageId,
                            finalRewriteQuery,
                            Map.of("inputTokens", inTokens, "outputTokens", outTokens, "totalTokens", inTokens + outTokens),
                            Map.of("retrieveCostMs", retrieveCostMs, "llmCostMs", llmCostMs, "totalCostMs", totalCostMs)
                    );
                    sendEvent(emitter, "message_end", endPayload);
                    sendEvent(emitter, "done", "[DONE]");

                    try {
                        emitter.complete();
                    } catch (Exception ignored) {
                    }
                    isCompleted.set(true);

                    // 异步持久化问答与更新 Redis 记忆
                    messageService.saveMessagePairAsync(sessionId, userId, query, fullAnswer,
                            finalRewriteQuery, sources, inTokens, outTokens, retrieveCostMs, llmCostMs, totalCostMs);
                    memoryManager.appendMessage(sessionId, "user", query);
                    memoryManager.appendMessage(sessionId, "assistant", fullAnswer);

                    log.info("[StreamChat] 流式问答生成完成：sessionId={}, tokens={}, 耗时={}ms",
                            sessionId, inTokens + outTokens, totalCostMs);
                }

                @Override
                public void onError(Throwable error) {
                    if (isCompleted.get()) {
                        return;
                    }
                    log.error("[StreamChat] 大模型流式推理失败：sessionId={}, 错误={}", sessionId, error.getMessage(), error);
                    sendEvent(emitter, "error", Map.of("code", 50001, "message", "大模型处理异常: " + error.getMessage()));
                    try {
                        emitter.completeWithError(error);
                    } catch (Exception ignored) {
                    }
                    isCompleted.set(true);
                }
            });

        } catch (Exception e) {
            log.error("[StreamChat] 流式问答编排失败：sessionId={}, 错误={}", sessionId, e.getMessage(), e);
            sendEvent(emitter, "error", Map.of("code", 50000, "message", "流式会话启动失败: " + e.getMessage()));
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().name(name).data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            log.debug("[StreamChat] SSE 发送事件失败（客户端已断连）：event={}, 错误={}", name, e.getMessage());
        } catch (Exception e) {
            log.warn("[StreamChat] SSE 发送未知异常：event={}, 错误={}", name, e.getMessage());
        }
    }
}
