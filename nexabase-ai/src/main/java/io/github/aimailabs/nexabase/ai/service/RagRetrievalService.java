package io.github.aimailabs.nexabase.ai.service;

import io.github.aimailabs.nexabase.ai.dto.FusedHit;

import java.util.List;

/**
 * RAG 多路召回编排服务。
 */
public interface RagRetrievalService {

    /**
     * 多路召回 + RRF 融合。
     *
     * @param query 用户问题
     * @param kbId  知识库过滤（可空）
     * @param topK  最终返回条数
     * @return 融合后的命中列表
     */
    List<FusedHit> retrieve(String query, Long kbId, int topK);
}
