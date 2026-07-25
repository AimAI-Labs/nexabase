package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "RAG 问答请求")
public class RagChatRequest {

    @Schema(description = "用户问题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @Schema(description = "知识库ID（可选过滤）")
    private Long kbId;

    @Schema(description = "召回融合条数", defaultValue = "5")
    private int topK = 5;
}
