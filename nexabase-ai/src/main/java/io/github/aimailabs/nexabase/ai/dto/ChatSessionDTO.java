package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会话信息 DTO。
 */
@Data
@Schema(description = "AI 会话信息 DTO")
public class ChatSessionDTO {

    @Schema(description = "会话业务唯一 UUID", example = "sess_8a7f6c3b2e")
    private String sessionId;

    @Schema(description = "所属用户 ID", example = "10001")
    private Long userId;

    @Schema(description = "会话标题", example = "Spring Cloud 架构规范咨询")
    private String title;

    @Schema(description = "绑定的知识库 ID", example = "1001")
    private Long kbId;

    @Schema(description = "绑定的模型名称", example = "qwen-plus")
    private String modelName;

    @Schema(description = "自定义 System Prompt")
    private String systemPrompt;

    @Schema(description = "采样温度", example = "0.70")
    private BigDecimal temperature;

    @Schema(description = "是否置顶 (0: 否, 1: 是)", example = "1")
    private Integer isPinned;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
