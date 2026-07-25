package io.github.aimailabs.nexabase.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "BM25 关键词检索请求")
public class Bm25SearchRequest {

    @Schema(description = "检索关键词", requiredMode = Schema.RequiredMode.REQUIRED)
    private String query;

    @Schema(description = "知识库ID（可选过滤）")
    private Long kbId;

    @Schema(description = "目录ID（可选过滤）")
    private Long categoryId;

    @Schema(description = "页码（从0开始）", defaultValue = "0")
    private int page = 0;

    @Schema(description = "每页大小", defaultValue = "10")
    private int size = 10;
}
