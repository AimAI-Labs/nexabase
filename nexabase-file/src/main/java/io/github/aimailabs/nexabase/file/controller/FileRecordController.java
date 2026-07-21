package io.github.aimailabs.nexabase.file.controller;

import io.github.aimailabs.nexabase.file.entity.FileRecord;
import io.github.aimailabs.nexabase.file.service.FileRecordService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "文件管理", description = "文件上传、下载与删除等操作")
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileRecordController {

    private final FileRecordService fileRecordService;

    @Operation(summary = "上传文件", description = "上传文件至对象存储并记录文件信息")
    @PostMapping("/upload")
    public Result<FileRecord> upload(
            @Parameter(description = "上传的文件对象", required = true) @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            Long userId = 1L; // TODO 暂未集成 Gateway X-User-Id 透传，默认 1L
            Long tenantId = 1L; 
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                userId = Long.parseLong(userIdStr);
            }
            FileRecord record = fileRecordService.upload(file, tenantId, userId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(500, "Upload failed: " + e.getMessage());
        }
    }

    @Operation(summary = "删除文件", description = "逻辑删除指定ID的文件记录，后台异步物理清理")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "文件记录ID", required = true) @PathVariable Long id, 
            HttpServletRequest request) {
        Long userId = 1L;
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            userId = Long.parseLong(userIdStr);
        }
        fileRecordService.deleteFile(id, userId);
        return Result.success(null);
    }
}
