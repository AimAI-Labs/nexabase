package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "RAG 引用来源")
public class RagSource {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "引用片段")
    private String snippet;

    @Schema(description = "相关性得分")
    private double score;

    @Schema(description = "召回来源：VECTOR/BM25")
    private String source;
}
