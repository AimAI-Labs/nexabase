package io.github.aimailabs.nexabase.ai.service.impl;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.github.aimailabs.nexabase.ai.chunk.SentenceTextSplitter;
import io.github.aimailabs.nexabase.ai.service.DocumentVectorizationService;
import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Common.Condition;
import io.qdrant.client.grpc.Common.FieldCondition;
import io.qdrant.client.grpc.Common.Filter;
import io.qdrant.client.grpc.Common.Match;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档向量化服务实现。
 * <p>写入用 EmbeddingStore.addAll（LC4j 框架收口）；删除用原生 QdrantClient 按 docId payload 过滤。
 * UPDATE 时先删后插，保证分块数变化无残留；重复消费幂等。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVectorizationServiceImpl implements DocumentVectorizationService {

    private final QwenEmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final QdrantClient qdrantClient;
    private final SentenceTextSplitter splitter = new SentenceTextSplitter(800, 200);

    @Value("${langchain4j.qdrant.collection}")
    private String collection;

    @Override
    public void vectorize(DocumentFullDTO doc) {
        if (doc == null || doc.getDocId() == null) {
            log.warn("向量化请求跳过：doc 为空");
            return;
        }
        if (doc.getStatus() == null || doc.getStatus() != 1) {
            log.info("文档 docId={} 状态为草稿(status={})，跳过向量化", doc.getDocId(), doc.getStatus());
            return;
        }
        if (doc.getContent() == null || doc.getContent().isBlank()) {
            log.warn("文档 docId={} 正文为空，跳过向量化", doc.getDocId());
            return;
        }

        Long docId = doc.getDocId();
        // 先删旧 chunk（处理 UPDATE 分块数变化），CREATE 时无 chunk 也安全
        deleteByDocId(docId);

        // 分块
        Document document = Document.from(doc.getContent());
        List<TextSegment> segments = splitter.split(document);

        // 注入业务 metadata（docId 用字符串，便于 Qdrant payload keyword 过滤）
        String docIdStr = String.valueOf(docId);
        String kbIdStr = doc.getKbId() == null ? null : String.valueOf(doc.getKbId());
        String title = doc.getTitle();
        List<TextSegment> enriched = new ArrayList<>(segments.size());
        for (TextSegment s : segments) {
            Metadata meta = s.metadata().put("docId", docIdStr);
            if (kbIdStr != null) {
                meta = meta.put("kbId", kbIdStr);
            }
            if (title != null) {
                meta = meta.put("title", title);
            }
            enriched.add(TextSegment.from(s.text(), meta));
        }

        // 批量 Embedding + 写入
        List<Embedding> embeddings = embeddingModel.embedAll(enriched).content();
        ensureCollectionExists();
        embeddingStore.addAll(embeddings, enriched);
        log.info("文档 docId={} 向量化完成，共 {} 块", docId, enriched.size());
    }

    private void ensureCollectionExists() {
        try {
            Boolean exists = qdrantClient.collectionExistsAsync(collection).get();
            if (!Boolean.TRUE.equals(exists)) {
                VectorParams vectorParams = VectorParams.newBuilder()
                        .setSize(1024)
                        .setDistance(Distance.Cosine)
                        .build();
                qdrantClient.createCollectionAsync(collection, vectorParams).get();
                log.info("Qdrant 向量集合 {} 自动创建成功 (1024 维, Cosine 距离)", collection);
            }
        } catch (Exception e) {
            log.warn("检查/自动创建 Qdrant 集合 {} 失败", collection, e);
        }
    }

    @Override
    public void deleteByDocId(Long docId) {
        if (docId == null) {
            return;
        }
        try {
            Boolean exists = qdrantClient.collectionExistsAsync(collection).get();
            if (Boolean.FALSE.equals(exists)) {
                log.info("Qdrant 集合 {} 尚不存在，跳过删除 docId={}", collection, docId);
                return;
            }
            FieldCondition fieldCondition = FieldCondition.newBuilder()
                    .setKey("docId")
                    .setMatch(Match.newBuilder().setKeyword(String.valueOf(docId)).build())
                    .build();
            Condition condition = Condition.newBuilder().setField(fieldCondition).build();
            Filter filter = Filter.newBuilder().addMust(condition).build();
            qdrantClient.deleteAsync(collection, filter).get();
            log.info("文档 docId={} 的 Qdrant chunk 已删除", docId);
        } catch (Exception e) {
            if (isNotFoundException(e)) {
                log.info("Qdrant 集合 {} 不存在或无对应 chunk，跳过删除 docId={}", collection, docId);
                return;
            }
            throw new RuntimeException("删除 Qdrant chunk 失败: docId=" + docId, e);
        }
    }

    private boolean isNotFoundException(Throwable t) {
        while (t != null) {
            if (t instanceof io.grpc.StatusRuntimeException statusEx) {
                if (statusEx.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                    return true;
                }
            }
            if (t.getMessage() != null && t.getMessage().contains("doesn't exist")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
