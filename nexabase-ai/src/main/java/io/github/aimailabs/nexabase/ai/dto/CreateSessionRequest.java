package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建会话请求。
 */
@Data
@Schema(description = "创建会话请求")
public class CreateSessionRequest {

    @Schema(description = "会话标题", example = "Spring Cloud 架构规范咨询")
    private String title;

    @Schema(description = "绑定的知识库 ID (可选，null 表示全局问答)", example = "1001")
    private Long kbId;

    @Schema(description = "绑定的模型名称 (可选，默认 qwen-plus)", example = "qwen-plus")
    private String modelName;

    @Schema(description = "自定义 System Prompt (可选)")
    private String systemPrompt;

    @Schema(description = "采样温度 (可选，默认 0.70)", example = "0.70")
    private BigDecimal temperature;
}
