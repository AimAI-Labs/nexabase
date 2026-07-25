package io.github.aimailabs.nexabase.document.controller;

import io.github.aimailabs.nexabase.document.api.dto.DocumentFullDTO;
import io.github.aimailabs.nexabase.document.entity.DocContent;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.mapper.DocInfoMapper;
import io.github.aimailabs.nexabase.document.repository.DocContentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 文档内部接口 / 文档内部接口
 * <p>供 nexabase-search / nexabase-ai 经 Feign 回调拉取文档完整视图。
 * 路径 /internal/** 走服务间 lb 直连，不经网关。
 *
 * @module nexabase-document
 */
@Tag(name = "文档内部接口", description = "服务间内部调用：拉取文档元数据与正文")
@RestController
@RequestMapping("/api/v1/document/internal")
@RequiredArgsConstructor
public class InternalDocumentController {

    private final DocInfoMapper docInfoMapper;
    private final DocContentRepository docContentRepository;

    /**
     * 拉取文档完整视图（元数据 + 正文）。
     *
     * @param documentId 文档ID
     * @return 文档完整视图；文档不存在或已逻辑删除时返回 null
     */
    @Operation(summary = "拉取文档完整视图", description = "合并 doc_info 元数据与 doc_content 正文，供索引/向量化消费")
    @GetMapping("/documents/{documentId}/full")
    public DocumentFullDTO getDocumentFull(@PathVariable Long documentId) {
        DocInfo doc = docInfoMapper.selectById(documentId);
        if (doc == null || doc.getIsDeleted() == 1) {
            return null;
        }
        Optional<DocContent> contentOpt = docContentRepository.findById(documentId);
        return new DocumentFullDTO(
                doc.getId(),
                doc.getTitle(),
                contentOpt.map(DocContent::getContent).orElse(null),
                doc.getKbId(),
                doc.getCategoryId(),
                doc.getTenantId(),
                doc.getStatus(),
                doc.getUpdatedAt()
        );
    }
}
