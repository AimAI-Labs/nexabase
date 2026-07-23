package io.github.aimailabs.nexabase.document.controller;

import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.service.DocInfoService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文档管理 / 文档管理接口
 * <p>
 * 提供文档的创建、查询、更新与删除功能，
 * 所有变更操作会自动投递事件至 MQ 触发向量化与索引同步。
 *
 * @module nexabase-document
 */
@Tag(name = "文档管理", description = "文档信息的创建、查询、更新与删除")
@RestController
@RequestMapping("/api/v1/document/doc")
@RequiredArgsConstructor
public class DocInfoController {

    private final DocInfoService docInfoService;

    /**
     * 创建文档，并自动推送到 MQ 进行异步分块处理。
     *
     * @param req 创建文档请求体（文档元数据 + 正文内容）
     * @return 创建成功的文档信息
     */
    @Operation(summary = "创建文档", description = "创建新文档，并自动推送到 MQ 进行异步分块处理")
    @PostMapping
    public Result<DocInfo> create(@RequestBody CreateDocRequest req) {
        Long userId = UserContext.getCurrentUserId();
        req.getDocInfo().setAuthorId(userId != null ? userId : 1L);
        return Result.success(docInfoService.create(req.getDocInfo(), req.getContent()));
    }

    /**
     * 更新文档内容与元数据，并触发重新向量化。
     *
     * @param id  文档 ID
     * @param req 更新文档请求体
     * @return 更新后的文档信息
     */
    @Operation(summary = "更新文档", description = "更新文档内容与元数据，并触发重新向量化")
    @PutMapping("/{id}")
    public Result<DocInfo> update(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id,
            @RequestBody UpdateDocRequest req) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(docInfoService.update(id, req.getDocInfo(), req.getContent(), userId != null ? userId : 1L));
    }

    /**
     * 根据 ID 获取文档详细信息。
     *
     * @param id 文档 ID
     * @return 文档详情
     */
    @Operation(summary = "获取文档详情", description = "根据ID获取文档详细信息")
    @GetMapping("/{id}")
    public Result<DocInfo> get(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        return Result.success(docInfoService.getById(id));
    }

    /**
     * 逻辑删除指定文档，并级联处理。
     *
     * @param id 文档 ID
     * @return 成功响应
     */
    @Operation(summary = "删除文档", description = "逻辑删除指定文档，并级联处理")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        docInfoService.delete(id, userId != null ? userId : 1L);
        return Result.success();
    }

    @Data
    @Schema(description = "创建文档请求体")
    public static class CreateDocRequest {
        @Schema(description = "文档元数据", requiredMode = Schema.RequiredMode.REQUIRED)
        private DocInfo docInfo;
        @Schema(description = "文档正文内容", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;
    }

    @Data
    @Schema(description = "更新文档请求体")
    public static class UpdateDocRequest {
        @Schema(description = "文档元数据")
        private DocInfo docInfo;
        @Schema(description = "文档正文内容")
        private String content;
    }
}
