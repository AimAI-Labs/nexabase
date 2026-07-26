package io.github.aimailabs.nexabase.ai.service.impl;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.feign.SearchBm25Client;
import io.github.aimailabs.nexabase.ai.service.RagRetrievalService;
import io.github.aimailabs.nexabase.ai.service.RrfFusionService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 多路召回编排：向量路(QdrantEmbeddingStore) + BM25 路(Feign→search) + RRF 融合。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalServiceImpl implements RagRetrievalService {

    private static final int PER_PATH_LIMIT = 10;
    private static final int RRF_K = 60;

    private final QwenEmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final SearchBm25Client searchBm25Client;
    private final RrfFusionService rrfFusionService;

    @Override
    public List<FusedHit> retrieve(String query, Long kbId, int topK) {
        List<FusedHit> vectorHits = vectorSearch(query);
        List<FusedHit> bm25Hits = bm25Search(query, kbId);
        List<FusedHit> fused = rrfFusionService.fuse(vectorHits, bm25Hits, RRF_K, topK);
        log.info("RAG 召回：vector={}, bm25={}, 融合后 top{}", vectorHits.size(), bm25Hits.size(), fused.size());
        return fused;
    }

    private List<FusedHit> vectorSearch(String query) {
        try {
            Embedding embedding = embeddingModel.embed(query).content();
            if (embedding == null || embedding.vector() == null || embedding.vector().length == 0) {
                log.warn("Query 向量计算失败或向量长度为0，跳过向量召回");
                return List.of();
            }
            EmbeddingSearchRequest req = EmbeddingSearchRequest.builder()
                    .queryEmbedding(embedding)
                    .maxResults(PER_PATH_LIMIT)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(req).matches();
            List<FusedHit> hits = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> m : matches) {
                Map<String, Object> meta = m.embedded().metadata().toMap();
                Object docId = meta.get("docId");
                Object title = meta.get("title");
                hits.add(new FusedHit(
                        docId == null ? null : Long.valueOf(docId.toString()),
                        title == null ? null : title.toString(),
                        m.embedded().text(),
                        m.score(),
                        "VECTOR"));
            }
            return hits;
        } catch (Exception e) {
            log.error("向量召回失败", e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<FusedHit> bm25Search(String query, Long kbId) {
        try {
            Map<String, Object> req = new HashMap<>();
            req.put("query", query);
            req.put("size", PER_PATH_LIMIT);
            if (kbId != null) {
                req.put("kbId", kbId);
            }
            Result<Map<String, Object>> resp = searchBm25Client.bm25Search(req);
            if (resp == null || resp.getData() == null) {
                return List.of();
            }
            Object hitsObj = resp.getData().get("hits");
            if (!(hitsObj instanceof List<?> hitsList)) {
                return List.of();
            }
            List<FusedHit> hits = new ArrayList<>();
            for (Object o : hitsList) {
                if (!(o instanceof Map<?, ?> h)) {
                    continue;
                }
                Object docId = h.get("docId");
                Object title = h.get("title");
                Object score = h.get("score");
                Object fragments = h.get("highlightFragments");
                String snippet = (fragments instanceof List<?> fl && !fl.isEmpty()) ? fl.get(0).toString() : "";
                hits.add(new FusedHit(
                        docId == null ? null : Long.valueOf(docId.toString()),
                        title == null ? null : title.toString(),
                        snippet,
                        score == null ? 0.0 : Double.parseDouble(score.toString()),
                        "BM25"));
            }
            return hits;
        } catch (Exception e) {
            log.error("BM25 召回失败", e);
            return List.of();
        }
    }
}
