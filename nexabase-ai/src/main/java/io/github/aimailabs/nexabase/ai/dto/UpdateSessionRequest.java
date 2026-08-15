package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新会话配置请求。
 */
@Data
@Schema(description = "更新会话配置请求")
public class UpdateSessionRequest {

    @Schema(description = "会话标题", example = "微服务与 Sentinel 规范")
    private String title;

    @Schema(description = "是否置顶 (0: 否, 1: 是)", example = "1")
    private Integer isPinned;

    @Schema(description = "绑定的知识库 ID", example = "1002")
    private Long kbId;

    @Schema(description = "绑定的模型名称", example = "qwen-plus")
    private String modelName;

    @Schema(description = "自定义 System Prompt")
    private String systemPrompt;

    @Schema(description = "采样温度", example = "0.50")
    private BigDecimal temperature;
}
