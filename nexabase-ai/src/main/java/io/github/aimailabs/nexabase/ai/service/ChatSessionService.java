package io.github.aimailabs.nexabase.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.ai.dto.ChatSessionDTO;
import io.github.aimailabs.nexabase.ai.dto.CreateSessionRequest;
import io.github.aimailabs.nexabase.ai.dto.UpdateSessionRequest;
import io.github.aimailabs.nexabase.ai.entity.ChatSession;

/**
 * AI 会话管理服务。
 */
public interface ChatSessionService {

    /**
     * 创建新会话。
     *
     * @param request 创建参数
     * @param userId  当前操作用户 ID
     * @return 会话信息 DTO
     */
    ChatSessionDTO createSession(CreateSessionRequest request, Long userId);

    /**
     * 分页查询当前用户的会话列表（置顶优先，更新时间倒序）。
     *
     * @param userId 当前用户 ID
     * @param kbId   可选知识库过滤
     * @param page   页码
     * @param size   每页大小
     * @return 会话分页对象
     */
    Page<ChatSessionDTO> listSessions(Long userId, Long kbId, int page, int size);

    /**
     * 更新会话配置（标题、置顶、知识库绑定、模型参数）。
     *
     * @param sessionId 会话 UUID
     * @param request   更新参数
     * @param userId    当前用户 ID
     * @return 是否成功
     */
    boolean updateSession(String sessionId, UpdateSessionRequest request, Long userId);

    /**
     * 逻辑删除会话及其历史，并驱逐缓存。
     *
     * @param sessionId 会话 UUID
     * @param userId    当前用户 ID
     * @return 是否成功
     */
    boolean deleteSession(String sessionId, Long userId);

    /**
     * 获取或自动创建会话（用于流式问答免显式创建会话）。
     *
     * @param sessionId   会话 UUID (若为空则新建)
     * @param userId      当前用户 ID
     * @param defaultKbId 默认绑定的知识库 ID
     * @return 会话实体
     */
    ChatSession getOrCreateSession(String sessionId, Long userId, Long defaultKbId);
}
