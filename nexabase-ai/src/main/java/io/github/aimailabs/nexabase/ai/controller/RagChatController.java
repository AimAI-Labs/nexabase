package io.github.aimailabs.nexabase.ai.controller;

import io.github.aimailabs.nexabase.ai.assistant.RagAssistant;
import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.dto.RagChatRequest;
import io.github.aimailabs.nexabase.ai.dto.RagChatResponse;
import io.github.aimailabs.nexabase.ai.dto.RagSource;
import io.github.aimailabs.nexabase.ai.prompt.RagPromptTemplate;
import io.github.aimailabs.nexabase.ai.service.RagRetrievalService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RAG 问答 / 同步问答接口
 *
 * @module nexabase-ai
 */
@Slf4j
@Tag(name = "RAG 问答", description = "BM25+向量多路召回 RRF 融合 + 大模型生成")
@RestController
@RequestMapping("/api/v1/ai/rag")
@RequiredArgsConstructor
public class RagChatController {

    private final RagRetrievalService retrievalService;
    private final RagAssistant ragAssistant;

    /**
     * 同步 RAG 问答，返回完整回答与引用来源。
     *
     * @param request 问答请求
     * @return 回答与引用来源
     */
    @Operation(summary = "同步 RAG 问答", description = "多路召回融合后注入上下文，大模型生成回答")
    @PostMapping("/chat")
    public Result<RagChatResponse> chat(@RequestBody RagChatRequest request) {
        String query = request.getQuery();
        Long kbId = request.getKbId();
        int topK = request.getTopK();
        log.info("[RAG-Chat] 收到同步问答请求：query=\"{}\", kbId={}, topK={}", query, kbId, topK);

        StopWatch sw = new StopWatch("RAG-Chat");
        try {
            // 1. 多路召回融合
            sw.start("retrieval");
            List<FusedHit> hits = retrievalService.retrieve(query, kbId, topK);
            sw.stop();
            log.info("[RAG-Chat] 检索阶段完成：命中条数={}, 耗时={}ms", hits.size(), sw.getLastTaskTimeMillis());

            // 2. Prompt 构建
            sw.start("prompt");
            String userMessage = RagPromptTemplate.buildUserMessage(query, hits);
            sw.stop();
            log.info("[RAG-Chat] Prompt 构建完成：上下文片段数={}, prompt长度={}字符, 耗时={}ms",
                    hits.size(), userMessage.length(), sw.getLastTaskTimeMillis());

            // 3. 大模型生成
            sw.start("llm");
            String answer;
            try {
                answer = ragAssistant.chat(userMessage);
            } catch (Exception e) {
                log.error("[RAG-Chat] 大模型调用失败：query=\"{}\", prompt长度={}字符, 已耗时={}ms, 错误={}",
                        query, userMessage.length(), sw.lastTaskInfo().getTimeMillis(), e.getMessage(), e);
                throw e;
            }
            sw.stop();
            log.info("[RAG-Chat] 大模型生成完成：回答长度={}字符, 耗时={}ms",
                    answer == null ? 0 : answer.length(), sw.getLastTaskTimeMillis());

            // 4. 组装响应
            RagChatResponse resp = new RagChatResponse();
            resp.setAnswer(answer);
            resp.setSources(hits.stream().map(h -> {
                RagSource s = new RagSource();
                s.setDocId(h.getDocId());
                s.setTitle(h.getTitle());
                s.setSnippet(h.getSnippet());
                s.setScore(h.getScore());
                s.setSource(h.getSource());
                return s;
            }).toList());

            log.info("[RAG-Chat] 同步问答成功：query=\"{}\", 召回={}, 引用={}, 总耗时={}ms",
                    query, hits.size(), resp.getSources().size(), sw.getTotalTimeMillis());
            return Result.success(resp);
        } catch (Exception e) {
            // 已在上方记录大模型调用失败详情，此处仅记录整体失败摘要，避免重复堆栈
            if (sw.isRunning()) {
                sw.stop();
            }
            log.error("[RAG-Chat] 同步问答失败：query=\"{}\", 已耗时={}ms, 错误={}",
                    query, sw.getTotalTimeMillis(), e.getMessage());
            throw e;
        }
    }
}
