package io.github.aimailabs.nexabase.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "BM25 检索响应")
public class Bm25SearchResponse {

    @Schema(description = "命中总数")
    private long total;

    @Schema(description = "命中列表")
    private List<Bm25Hit> hits;
}
