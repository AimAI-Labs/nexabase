package io.github.aimailabs.nexabase.ai.consumer;

import io.github.aimailabs.nexabase.ai.service.DocumentVectorizationService;
import io.github.aimailabs.nexabase.document.api.client.DocumentInternalApi;
import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;
import io.github.aimailabs.nexabase.document.api.event.DocumentChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * AI 向量化消费者。
 * <p>监听 {@code document.queue.ai}，根据 action 分块向量化或删除 chunk。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiIndexConsumer {

    private final DocumentInternalApi documentInternalApi;
    private final DocumentVectorizationService vectorizationService;

    @RabbitListener(queues = "document.queue.ai")
    public void onDocumentChanged(DocumentChangedEvent event) {
        log.info("收到文档变更事件：docId={}, action={}", event.getDocumentId(), event.getAction());
        Long docId = event.getDocumentId();
        String action = event.getAction();
        try {
            switch (action) {
                case "CREATE", "UPDATE" -> {
                    DocumentFullDTO doc = documentInternalApi.getDocumentFull(docId);
                    vectorizationService.vectorize(doc);
                }
                case "DELETE" -> vectorizationService.deleteByDocId(docId);
                default -> log.warn("未知 action={}，跳过 docId={}", action, docId);
            }
        } catch (Exception e) {
            log.error("向量化处理失败：docId={}, action={}", docId, action, e);
            throw new RuntimeException("向量化处理失败: " + docId, e);
        }
    }
}
