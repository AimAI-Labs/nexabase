package io.github.aimailabs.nexabase.search.service.impl;

import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;
import io.github.aimailabs.nexabase.search.entity.EsDoc;
import io.github.aimailabs.nexabase.search.service.DocumentIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

/**
 * 文档 ES 索引服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIndexServiceImpl implements DocumentIndexService {

    private final ElasticsearchOperations operations;

    @Override
    public void index(DocumentFullDTO doc) {
        if (doc == null || doc.getDocId() == null) {
            log.warn("索引请求跳过：doc 为空");
            return;
        }
        // 草稿不索引
        if (doc.getStatus() == null || doc.getStatus() != 1) {
            log.info("文档 docId={} 状态为草稿(status={})，跳过 ES 索引", doc.getDocId(), doc.getStatus());
            return;
        }
        EsDoc esDoc = new EsDoc();
        esDoc.setDocId(doc.getDocId());
        esDoc.setTitle(doc.getTitle());
        esDoc.setContent(doc.getContent());
        esDoc.setKbId(doc.getKbId());
        esDoc.setCategoryId(doc.getCategoryId());
        esDoc.setTenantId(doc.getTenantId());
        esDoc.setStatus(doc.getStatus());
        esDoc.setUpdatedAt(doc.getUpdatedAt());
        // save 按 @Id(docId) upsert，天然幂等
        operations.save(esDoc);
        log.info("文档 docId={} 已索引到 ES", doc.getDocId());
    }

    @Override
    public void deleteByDocId(Long docId) {
        if (docId == null) {
            return;
        }
        String id = String.valueOf(docId);
        operations.delete(id, EsDoc.class);
        log.info("文档 docId={} 已从 ES 删除", docId);
    }
}
