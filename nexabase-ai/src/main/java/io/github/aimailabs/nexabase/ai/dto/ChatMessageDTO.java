package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息明细 DTO。
 */
@Data
@Schema(description = "AI 消息明细 DTO")
public class ChatMessageDTO {

    @Schema(description = "消息业务唯一 UUID", example = "msg_res_9b4e1a")
    private String messageId;

    @Schema(description = "关联会话 UUID", example = "sess_8a7f6c3b2e")
    private String sessionId;

    @Schema(description = "提问用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "角色: user / assistant / system", example = "assistant")
    private String role;

    @Schema(description = "消息正文")
    private String content;

    @Schema(description = "改写后的独立检索 Query")
    private String rewriteQuery;

    @Schema(description = "引用的知识库切片快照列表")
    private List<RagSource> citations;

    @Schema(description = "输入 Token 消耗", example = "428")
    private Integer inputTokens;

    @Schema(description = "输出 Token 消耗", example = "165")
    private Integer outputTokens;

    @Schema(description = "检索与融合耗时(ms)", example = "45")
    private Integer retrieveCostMs;

    @Schema(description = "大模型生成耗时(ms)", example = "1250")
    private Integer llmCostMs;

    @Schema(description = "总端到端耗时(ms)", example = "1320")
    private Integer totalCostMs;

    @Schema(description = "用户反馈(0:无反馈, 1:点赞, 2:点踩)", example = "1")
    private Integer feedbackStatus;

    @Schema(description = "用户反馈备注/原因")
    private String feedbackRemark;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
