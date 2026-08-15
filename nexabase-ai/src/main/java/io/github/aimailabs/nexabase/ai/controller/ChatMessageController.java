package io.github.aimailabs.nexabase.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.ai.dto.ChatMessageDTO;
import io.github.aimailabs.nexabase.ai.dto.FeedbackRequest;
import io.github.aimailabs.nexabase.ai.service.ChatMessageService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话消息管理接口
 *
 * @module nexabase-ai
 */
@Slf4j
@Tag(name = "AI 对话消息", description = "历史消息查询与用户反馈")
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService messageService;

    /**
     * 分页查询会话历史消息。
     *
     * @param sessionId 会话 UUID
     * @param page      页码
     * @param size      每页数量
     * @return 消息列表分页
     */
    @Operation(summary = "查询历史消息", description = "获取指定会话的历史消息记录，包含引用切片快照")
    @GetMapping("/session/{sessionId}/messages")
    public Result<Page<ChatMessageDTO>> listMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = UserContext.getCurrentUserId();
        Page<ChatMessageDTO> result = messageService.listMessages(sessionId, userId, page, size);
        return Result.success(result);
    }

    /**
     * 对消息进行点赞/点踩反馈。
     *
     * @param messageId 消息 UUID
     * @param request   反馈请求
     * @return 是否成功
     */
    @Operation(summary = "消息反馈", description = "为指定的回答消息进行点赞或点踩评价")
    @PostMapping("/message/{messageId}/feedback")
    public Result<Boolean> feedback(
            @PathVariable String messageId,
            @RequestBody FeedbackRequest request) {
        Long userId = UserContext.getCurrentUserId();
        boolean success = messageService.updateFeedback(messageId, request, userId);
        return Result.success(success);
    }
}
