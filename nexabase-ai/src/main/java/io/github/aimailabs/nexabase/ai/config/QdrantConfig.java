package io.github.aimailabs.nexabase.ai.config;

import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qdrant 配置。
 * <p>{@link QdrantEmbeddingStore} 收口向量写入与召回；原生 {@link QdrantClient} 仅用于按 docId payload 过滤删除 chunk。
 */
@Configuration
public class QdrantConfig {

    @Bean
    public QdrantEmbeddingStore qdrantEmbeddingStore(
            @Value("${langchain4j.qdrant.host}") String host,
            @Value("${langchain4j.qdrant.port}") int port,
            @Value("${langchain4j.qdrant.collection}") String collection) {
        // 首次写入自动创建集合（1024 维，Cosine）
        return QdrantEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(collection)
                .build();
    }

    @Bean(destroyMethod = "close")
    public QdrantClient qdrantClient(
            @Value("${langchain4j.qdrant.host}") String host,
            @Value("${langchain4j.qdrant.port}") int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        return new QdrantClient(QdrantGrpcClient.newBuilder(channel).build());
    }
}
