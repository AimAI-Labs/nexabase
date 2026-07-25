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
        List<FusedHit> hits = retrievalService.retrieve(request.getQuery(), request.getKbId(), request.getTopK());
        String userMessage = RagPromptTemplate.buildUserMessage(request.getQuery(), hits);
        String answer = ragAssistant.chat(userMessage);

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
        return Result.success(resp);
    }
}
