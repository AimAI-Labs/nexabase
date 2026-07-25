package io.github.aimailabs.nexabase.search.service;

import io.github.aimailabs.nexabase.search.dto.Bm25SearchRequest;
import io.github.aimailabs.nexabase.search.dto.Bm25SearchResponse;

/**
 * 文档 BM25 检索服务。
 */
public interface DocumentSearchService {

    Bm25SearchResponse bm25Search(Bm25SearchRequest request);
}
