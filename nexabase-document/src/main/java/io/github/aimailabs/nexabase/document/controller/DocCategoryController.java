package io.github.aimailabs.nexabase.document.controller;

import io.github.aimailabs.nexabase.document.entity.DocCategory;
import io.github.aimailabs.nexabase.document.service.DocCategoryService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档目录管理接口。
 * <p>
 * 提供目录的创建、查询与删除功能，支持无限层级树形结构。
 */
@Tag(name = "文档目录", description = "文档目录的创建、查询与删除")
@RestController
@RequestMapping("/api/v1/document/category")
@RequiredArgsConstructor
public class DocCategoryController {

    private final DocCategoryService categoryService;

    @Operation(summary = "创建目录", description = "在知识库中创建新目录，自动计算物化路径")
    @PostMapping
    public Result<DocCategory> create(@RequestBody DocCategory category) {
        return Result.success(categoryService.create(category));
    }

    @Operation(summary = "获取目录详情", description = "根据ID获取单个目录信息")
    @GetMapping("/{id}")
    public Result<DocCategory> get(
            @Parameter(description = "目录ID", required = true) @PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "查询知识库目录树", description = "获取指定知识库的完整目录树列表")
    @GetMapping("/tree/{kbId}")
    public Result<List<DocCategory>> listByKb(
            @Parameter(description = "知识库ID", required = true) @PathVariable Long kbId) {
        return Result.success(categoryService.listByKb(kbId));
    }

    @Operation(summary = "删除目录", description = "逻辑删除目录（需确保目录下无文档）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "目录ID", required = true) @PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        categoryService.delete(id, userId != null ? userId : 1L);
        return Result.success();
    }
}
