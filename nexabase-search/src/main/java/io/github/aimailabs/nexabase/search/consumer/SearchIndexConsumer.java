package io.github.aimailabs.nexabase.search.consumer;

import io.github.aimailabs.nexabase.document.api.client.DocumentInternalApi;
import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;
import io.github.aimailabs.nexabase.document.api.event.DocumentChangedEvent;
import io.github.aimailabs.nexabase.search.service.DocumentIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Search 索引消费者。
 * <p>监听 {@code document.queue.search}，根据 action 构建或删除 ES BM25 索引。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexConsumer {

    private final DocumentInternalApi documentInternalApi;
    private final DocumentIndexService documentIndexService;

    @RabbitListener(queues = "document.queue.search")
    public void onDocumentChanged(DocumentChangedEvent event) {
        log.info("收到文档变更事件：docId={}, action={}", event.getDocumentId(), event.getAction());
        Long docId = event.getDocumentId();
        String action = event.getAction();
        try {
            switch (action) {
                case "CREATE", "UPDATE" -> {
                    DocumentFullDTO doc = documentInternalApi.getDocumentFull(docId);
                    documentIndexService.index(doc);
                }
                case "DELETE" -> documentIndexService.deleteByDocId(docId);
                default -> log.warn("未知 action={}，跳过 docId={}", action, docId);
            }
        } catch (Exception e) {
            log.error("处理文档变更事件失败：docId={}, action={}", docId, action, e);
            // 抛出触发重试 → 耗尽后进 DLQ
            throw new RuntimeException("ES 索引处理失败: " + docId, e);
        }
    }
}
