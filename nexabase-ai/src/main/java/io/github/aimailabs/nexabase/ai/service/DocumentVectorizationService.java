package io.github.aimailabs.nexabase.ai.service;

import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;

/**
 * 文档向量化服务。
 */
public interface DocumentVectorizationService {

    /** 分块 + Embedding + Qdrant 写入（先按 docId 删旧 chunk 再 upsert 新 chunk，保证幂等） */
    void vectorize(DocumentFullDTO doc);

    /** 按 docId 删除 Qdrant 全部 chunk */
    void deleteByDocId(Long docId);
}
