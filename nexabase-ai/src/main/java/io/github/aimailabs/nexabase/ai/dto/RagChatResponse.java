package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "RAG 问答响应")
public class RagChatResponse {

    @Schema(description = "模型回答")
    private String answer;

    @Schema(description = "引用来源列表")
    private List<RagSource> sources;
}
