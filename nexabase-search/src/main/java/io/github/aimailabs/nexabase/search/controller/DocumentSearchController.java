package io.github.aimailabs.nexabase.search.controller;

import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.search.dto.Bm25SearchRequest;
import io.github.aimailabs.nexabase.search.dto.Bm25SearchResponse;
import io.github.aimailabs.nexabase.search.service.DocumentSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文档检索 / BM25 关键词召回接口
 *
 * @module nexabase-search
 */
@Tag(name = "文档检索", description = "Elasticsearch BM25 关键词召回与高亮")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final DocumentSearchService documentSearchService;

    /**
     * BM25 关键词检索，返回高亮命中。
     *
     * @param request 检索请求
     * @return 高亮命中列表
     */
    @Operation(summary = "BM25 关键词检索", description = "基于 Elasticsearch 倒排索引的关键词召回，支持 title/content 高亮与 kbId/categoryId 过滤")
    @PostMapping("/bm25")
    public Result<Bm25SearchResponse> bm25(@RequestBody Bm25SearchRequest request) {
        return Result.success(documentSearchService.bm25Search(request));
    }
}
