package io.github.aimailabs.nexabase.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "BM25 检索单条命中")
public class Bm25Hit {

    @Schema(description = "文档ID")
    private Long docId;

    @Schema(description = "文档标题")
    private String title;

    @Schema(description = "高亮片段")
    private List<String> highlightFragments;

    @Schema(description = "相关性得分")
    private float score;

    @Schema(description = "知识库ID")
    private Long kbId;
}
