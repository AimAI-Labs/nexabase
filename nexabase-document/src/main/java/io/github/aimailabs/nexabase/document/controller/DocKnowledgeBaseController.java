package io.github.aimailabs.nexabase.document.controller;

import io.github.aimailabs.nexabase.document.entity.DocKnowledgeBase;
import io.github.aimailabs.nexabase.document.service.DocKnowledgeBaseService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文档管理 / 知识库管理接口
 * <p>
 * 提供知识库的创建、查询与删除功能。
 *
 * @module nexabase-document
 */
@Tag(name = "知识库管理", description = "知识库的创建、查询与删除")
@RestController
@RequestMapping("/api/v1/document/kb")
@RequiredArgsConstructor
public class DocKnowledgeBaseController {

    private final DocKnowledgeBaseService kbService;

    /**
     * 创建知识库，并绑定当前操作人作为所有者。
     *
     * @param kb 知识库实体（名称、描述、是否公开等）
     * @return 创建成功的知识库信息
     */
    @Operation(summary = "创建知识库", description = "创建一个新的知识库集合")
    @PostMapping
    public Result<DocKnowledgeBase> create(@RequestBody DocKnowledgeBase kb) {
        Long userId = UserContext.getCurrentUserId();
        kb.setOwnerId(userId != null ? userId : 1L);
        return Result.success(kbService.create(kb));
    }

    /**
     * 根据知识库 ID 查询知识库详情。
     *
     * @param id 知识库 ID
     * @return 知识库详情
     */
    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库基本信息")
    @GetMapping("/{id}")
    public Result<DocKnowledgeBase> get(
            @Parameter(description = "知识库ID", required = true) @PathVariable Long id) {
        return Result.success(kbService.getById(id));
    }

    /**
     * 删除知识库（逻辑删除，级联其下所有分类与文档）。
     *
     * @param id 知识库 ID
     * @return 成功响应
     */
    @Operation(summary = "删除知识库", description = "逻辑删除知识库及其下所有分类和文档")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "知识库ID", required = true) @PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        kbService.delete(id, userId != null ? userId : 1L);
        return Result.success();
    }
}
