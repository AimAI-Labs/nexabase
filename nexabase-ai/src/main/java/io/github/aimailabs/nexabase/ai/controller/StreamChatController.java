package io.github.aimailabs.nexabase.ai.controller;

import io.github.aimailabs.nexabase.ai.dto.StreamChatRequest;
import io.github.aimailabs.nexabase.ai.service.StreamChatService;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
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

/**
 * AI 多轮流式对话接口
 *
 * @module nexabase-ai
 */
@Slf4j
@Tag(name = "AI 流式问答", description = "多轮上下文与 RAG 知识库问答 SSE 接口")
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class StreamChatController {

    private final StreamChatService streamChatService;

    /**
     * 多轮 SSE 流式问答接口。
     * <p>事件生命周期：thought(意图改写) -> citation(引用切片) -> message(逐Token) -> message_end(统计) -> done。
     *
     * @param request 问答请求参数
     * @return SseEmitter 事件流
     */
    @Operation(summary = "多轮流式对话", description = "基于 SSE 的多轮上下文与知识库 RAG 问答，输出细粒度生命周期事件流")
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody StreamChatRequest request) {
        Long userId = UserContext.getCurrentUserId();
        return streamChatService.chatStream(request, userId);
    }
}
