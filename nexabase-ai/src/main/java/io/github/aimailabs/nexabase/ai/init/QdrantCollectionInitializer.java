package io.github.aimailabs.nexabase.ai.init;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Qdrant 向量集合初始化器。
 * <p>应用启动时检查集合 {@code nexabase_doc_chunks} 是否存在，不存在则自动创建（1024 维，Cosine 距离）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QdrantCollectionInitializer implements ApplicationRunner {

    private final QdrantClient qdrantClient;

    @Value("${langchain4j.qdrant.collection}")
    private String collection;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Boolean exists = qdrantClient.collectionExistsAsync(collection).get();
            if (Boolean.TRUE.equals(exists)) {
                log.info("Qdrant 向量集合 {} 已存在，跳过创建", collection);
                return;
            }

            VectorParams vectorParams = VectorParams.newBuilder()
                    .setSize(1024)
                    .setDistance(Distance.Cosine)
                    .build();

            qdrantClient.createCollectionAsync(collection, vectorParams).get();
            log.info("Qdrant 向量集合 {} 创建成功 (1024 维, Cosine 距离)", collection);
        } catch (Exception e) {
            log.error("Qdrant 向量集合 {} 初始化失败", collection, e);
        }
    }
}
