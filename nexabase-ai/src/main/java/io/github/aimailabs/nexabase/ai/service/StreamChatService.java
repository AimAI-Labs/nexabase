package io.github.aimailabs.nexabase.ai.service;

import io.github.aimailabs.nexabase.ai.dto.StreamChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 多轮流式对话服务。
 */
public interface StreamChatService {

    /**
     * 发起多轮 SSE 流式问答。
     * <p>事件生命周期：thought(意图改写) -> citation(引用切片) -> message(逐Token) -> message_end(统计) -> done。
     *
     * @param request 请求参数
     * @param userId  操作用户 ID
     * @return SseEmitter 实例
     */
    SseEmitter chatStream(StreamChatRequest request, Long userId);
}
