package io.github.aimailabs.nexabase.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.ai.dto.ChatMessageDTO;
import io.github.aimailabs.nexabase.ai.dto.FeedbackRequest;
import io.github.aimailabs.nexabase.ai.dto.RagSource;

import java.util.List;

/**
 * AI 消息管理与持久化服务。
 */
public interface ChatMessageService {

    /**
     * 异步保存一轮问答记录 (User提问 + Assistant回答)。
     */
    void saveMessagePairAsync(String sessionId, Long userId, String userQuery, String assistantAnswer,
                              String rewriteQuery, List<RagSource> citations,
                              Integer inputTokens, Integer outputTokens,
                              Long retrieveCostMs, Long llmCostMs, Long totalCostMs);

    /**
     * 分页查询指定会话的历史消息。
     */
    Page<ChatMessageDTO> listMessages(String sessionId, Long userId, int page, int size);

    /**
     * 针对某条消息进行点赞/点踩反馈。
     */
    boolean updateFeedback(String messageId, FeedbackRequest request, Long userId);
}
