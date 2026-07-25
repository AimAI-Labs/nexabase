package io.github.aimailabs.nexabase.search.service;

import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;

/**
 * 文档 ES 索引服务。
 */
public interface DocumentIndexService {

    /** 索引(创建/更新)文档到 ES */
    void index(DocumentFullDTO doc);

    /** 按 docId 从 ES 删除文档 */
    void deleteByDocId(Long docId);
}
