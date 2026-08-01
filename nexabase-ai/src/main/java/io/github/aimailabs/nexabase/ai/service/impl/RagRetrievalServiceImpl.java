package io.github.aimailabs.nexabase.ai.service.impl;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import io.github.aimailabs.nexabase.ai.dto.FusedHit;
import io.github.aimailabs.nexabase.ai.feign.SearchBm25Client;
import io.github.aimailabs.nexabase.ai.service.RagRetrievalService;
import io.github.aimailabs.nexabase.ai.service.RrfFusionService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.SearchPoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 多路召回编排：向量路(QdrantClient 原生 gRPC search) + BM25 路(Feign→search) + RRF 融合。
 * <p>向量检索绕过 {@code QdrantEmbeddingStore.search}：该方法回读 stored vector 在客户端
 * 重算 cosine，但 Qdrant 1.18 服务端 gRPC 响应的 {@code ScoredPoint.vectors} 在当前
 * langchain4j-qdrant 1.9.1 + qdrant-java client 组合下被解析为空，导致
 * {@code CosineSimilarity.between} 抛出 {@code vector a (0) != vector b (1024)}。
 * <p>改为直接用服务端返回的 score（单向量 Cosine 距离下即 cosine similarity），
 * 不回读 vector 做客户端重算。RRF 融合仅依赖 rank 排名，score 绝对值不影响融合结果。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalServiceImpl implements RagRetrievalService {

    private static final int PER_PATH_LIMIT = 10;
    private static final int RRF_K = 60;
    private static final String PAYLOAD_TEXT_KEY = "text_segment";

    private final QwenEmbeddingModel embeddingModel;
    private final QdrantClient qdrantClient;
    private final SearchBm25Client searchBm25Client;
    private final RrfFusionService rrfFusionService;

    @Value("${langchain4j.qdrant.collection}")
    private String collection;

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

            SearchPoints req = SearchPoints.newBuilder()
                    .setCollectionName(collection)
                    .addAllVector(embedding.vectorAsList())
                    .setWithVectors(WithVectorsSelectorFactory.enable(true))
                    .setWithPayload(WithPayloadSelectorFactory.enable(true))
                    .setLimit(PER_PATH_LIMIT)
                    .build();

            List<ScoredPoint> results = qdrantClient.searchAsync(req).get();

            List<FusedHit> hits = new ArrayList<>();
            for (ScoredPoint sp : results) {
                Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = sp.getPayloadMap();
                io.qdrant.client.grpc.JsonWithInt.Value textVal = payload.getOrDefault(PAYLOAD_TEXT_KEY, null);
                String text = (textVal != null && !textVal.equals(io.qdrant.client.grpc.JsonWithInt.Value.getDefaultInstance()))
                        ? textVal.getStringValue() : "";
                Object docId = valueOf(payload.get("docId"));
                Object title = valueOf(payload.get("title"));
                hits.add(new FusedHit(
                        docId == null ? null : Long.valueOf(docId.toString()),
                        title == null ? null : title.toString(),
                        text,
                        sp.getScore(),
                        "VECTOR"));
            }
            return hits;
        } catch (Exception e) {
            log.error("向量召回失败", e);
            return List.of();
        }
    }

    /**
     * 提取 JsonWithInt.Value 的 Java 值（支持 string/double/integer）。
     * <p>注意：Qdrant payload 的 Value 类型是 {@link io.qdrant.client.grpc.JsonWithInt.Value}，
     * 区别于 {@link com.google.protobuf.Value}，它额外支持 INTEGER_VALUE 以避免大整数精度丢失。
     */
    private Object valueOf(io.qdrant.client.grpc.JsonWithInt.Value v) {
        if (v == null || v.equals(io.qdrant.client.grpc.JsonWithInt.Value.getDefaultInstance())) {
            return null;
        }
        switch (v.getKindCase()) {
            case STRING_VALUE -> { return v.getStringValue(); }
            case DOUBLE_VALUE -> { return v.getDoubleValue(); }
            case INTEGER_VALUE -> { return v.getIntegerValue(); }
            default -> { return null; }
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
