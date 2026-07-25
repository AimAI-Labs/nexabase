package io.github.aimailabs.nexabase.ai.service;

import io.github.aimailabs.nexabase.ai.dto.FusedHit;

import java.util.List;

/**
 * RRF 融合服务。
 */
public interface RrfFusionService {

    /**
     * RRF 融合两路召回。
     *
     * @param vectorHits 向量路命中（已按 score 降序）
     * @param bm25Hits   BM25 路命中（已按 score 降序）
     * @param k          RRF 平滑常数（通常 60）
     * @param topK       返回前 K 条
     * @return 融合后按 RRF 分降序的 topK 命中
     */
    List<FusedHit> fuse(List<FusedHit> vectorHits, List<FusedHit> bm25Hits, int k, int topK);
}
