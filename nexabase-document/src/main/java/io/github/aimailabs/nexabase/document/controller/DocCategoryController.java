package io.github.aimailabs.nexabase.document.controller;

import io.github.aimailabs.nexabase.document.entity.DocCategory;
import io.github.aimailabs.nexabase.document.service.DocCategoryService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "文档分类", description = "文档分类目录的创建、查询与删除")
@RestController
@RequestMapping("/api/v1/document/category")
@RequiredArgsConstructor
public class DocCategoryController {

    private final DocCategoryService categoryService;

    @Operation(summary = "创建分类", description = "在知识库中创建新分类")
    @PostMapping
    public Result<DocCategory> create(@RequestBody DocCategory category, HttpServletRequest request) {
        category.setCreatedBy(getUserId(request));
        return Result.success(categoryService.create(category));
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取单个分类信息")
    @GetMapping("/{id}")
    public Result<DocCategory> get(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "删除分类", description = "逻辑删除分类目录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "分类ID", required = true) @PathVariable Long id, 
            HttpServletRequest request) {
        categoryService.delete(id, getUserId(request));
        return Result.success(null);
    }

    private Long getUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        return (userIdStr != null && !userIdStr.isEmpty()) ? Long.parseLong(userIdStr) : 1L;
    }
}
