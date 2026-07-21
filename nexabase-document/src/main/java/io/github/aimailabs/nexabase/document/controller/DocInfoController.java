package io.github.aimailabs.nexabase.document.controller;

import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.service.DocInfoService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "文档管理", description = "文档信息的创建、查询与删除")
@RestController
@RequestMapping("/api/v1/document/doc")
@RequiredArgsConstructor
public class DocInfoController {

    private final DocInfoService docInfoService;

    @Operation(summary = "创建文档", description = "创建新文档，并自动推送到 MQ 进行异步分块处理")
    @PostMapping
    public Result<DocInfo> create(@RequestBody CreateDocRequest req, HttpServletRequest request) {
        req.getDocInfo().setCreatedBy(getUserId(request));
        req.getDocInfo().setAuthorId(getUserId(request));
        return Result.success(docInfoService.create(req.getDocInfo(), req.getContent()));
    }

    @Operation(summary = "获取文档详情", description = "根据ID获取文档详细信息")
    @GetMapping("/{id}")
    public Result<DocInfo> get(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id) {
        return Result.success(docInfoService.getById(id));
    }

    @Operation(summary = "删除文档", description = "逻辑删除指定文档，并级联处理")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "文档ID", required = true) @PathVariable Long id, 
            HttpServletRequest request) {
        docInfoService.delete(id, getUserId(request));
        return Result.success(null);
    }

    private Long getUserId(HttpServletRequest request) {
        String userIdStr = request.getHeader("X-User-Id");
        return (userIdStr != null && !userIdStr.isEmpty()) ? Long.parseLong(userIdStr) : 1L;
    }

    @Data
    @Schema(description = "创建文档请求体")
    public static class CreateDocRequest {
        @Schema(description = "文档元数据", requiredMode = Schema.RequiredMode.REQUIRED)
        private DocInfo docInfo;
        @Schema(description = "文档正文内容", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;
    }
}
