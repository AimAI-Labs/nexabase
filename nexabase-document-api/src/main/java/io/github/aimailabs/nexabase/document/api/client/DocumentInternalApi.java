package io.github.aimailabs.nexabase.document.api.client;

import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 文档服务内部 Feign 客户端。
 * <p>供 nexabase-search / nexabase-ai 消费者回调拉取文档正文与元数据。
 * 路径 {@code /api/v1/document/internal/**} 走服务间 lb 直连，不经过网关鉴权。
 */
@FeignClient(name = "nexabase-document", path = "/api/v1/document/internal")
public interface DocumentInternalApi {

    /**
     * 拉取文档完整视图（元数据 + 正文）。
     *
     * @param documentId 文档ID
     * @return 文档完整视图；不存在时返回 null（由调用方判空跳过）
     */
    @GetMapping("/documents/{documentId}/full")
    DocumentFullDTO getDocumentFull(@PathVariable("documentId") Long documentId);
}
