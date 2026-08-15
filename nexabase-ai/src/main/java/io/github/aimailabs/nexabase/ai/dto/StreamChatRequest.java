package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 多轮流式对话请求。
 */
@Data
@Schema(description = "多轮流式对话请求")
public class StreamChatRequest {

    @Schema(description = "会话 UUID (为空则后台自动新建会话并下发)", example = "sess_8a7f6c3b2e")
    private String sessionId;

    @Schema(description = "用户提问 Query", requiredMode = Schema.RequiredMode.REQUIRED, example = "它支持哪些部署模式？")
    private String query;

    @Schema(description = "覆盖知识库 ID (可选，默认使用 Session 绑定的知识库)", example = "1001")
    private Long kbId;

    @Schema(description = "召回切片数量 (可选，默认 5)", example = "5")
    private Integer topK = 5;

    @Schema(description = "采样温度 (可选，覆盖默认值)", example = "0.70")
    private BigDecimal temperature;
}
