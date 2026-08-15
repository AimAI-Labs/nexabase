package io.github.aimailabs.nexabase.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.ai.dto.ChatSessionDTO;
import io.github.aimailabs.nexabase.ai.dto.CreateSessionRequest;
import io.github.aimailabs.nexabase.ai.dto.UpdateSessionRequest;
import io.github.aimailabs.nexabase.ai.service.ChatSessionService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话会话管理接口
 *
 * @module nexabase-ai
 */
@Slf4j
@Tag(name = "AI 对话会话", description = "会话 CRUD 管理与配置")
@RestController
@RequestMapping("/api/v1/ai/chat")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService sessionService;

    /**
     * 创建新对话会话。
     *
     * @param request 创建参数
     * @return 会话 DTO
     */
    @Operation(summary = "创建会话", description = "创建新的对话会话，可选绑定知识库与模型参数")
    @PostMapping("/session")
    public Result<ChatSessionDTO> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        Long userId = UserContext.getCurrentUserId();
        ChatSessionDTO dto = sessionService.createSession(request, userId);
        return Result.success(dto);
    }

    /**
     * 分页查询当前用户的会话列表。
     *
     * @param page 页码
     * @param size 每页数量
     * @param kbId 知识库 ID 过滤
     * @return 会话分页列表
     */
    @Operation(summary = "查询会话列表", description = "获取当前用户的会话列表，置顶优先，更新时间倒序")
    @GetMapping("/sessions")
    public Result<Page<ChatSessionDTO>> listSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long kbId) {
        Long userId = UserContext.getCurrentUserId();
        Page<ChatSessionDTO> result = sessionService.listSessions(userId, kbId, page, size);
        return Result.success(result);
    }

    /**
     * 更新会话配置。
     *
     * @param sessionId 会话 UUID
     * @param request   更新参数
     * @return 是否成功
     */
    @Operation(summary = "更新会话配置", description = "修改会话标题、置顶状态、知识库绑定或模型参数")
    @PutMapping("/session/{sessionId}")
    public Result<Boolean> updateSession(
            @PathVariable String sessionId,
            @RequestBody UpdateSessionRequest request) {
        Long userId = UserContext.getCurrentUserId();
        boolean success = sessionService.updateSession(sessionId, request, userId);
        return Result.success(success);
    }

    /**
     * 逻辑删除会话。
     *
     * @param sessionId 会话 UUID
     * @return 是否成功
     */
    @Operation(summary = "删除会话", description = "逻辑删除会话及其历史消息，并清理内存缓存")
    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteSession(@PathVariable String sessionId) {
        Long userId = UserContext.getCurrentUserId();
        boolean success = sessionService.deleteSession(sessionId, userId);
        return Result.success(success);
    }
}
