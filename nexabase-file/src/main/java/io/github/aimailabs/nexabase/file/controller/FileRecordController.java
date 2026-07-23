package io.github.aimailabs.nexabase.file.controller;

import io.github.aimailabs.nexabase.file.entity.FileRecord;
import io.github.aimailabs.nexabase.file.service.FileRecordService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件服务 / 文件管理接口
 * <p>
 * 提供文件上传、下载与逻辑删除功能，所有操作自动记录操作人。
 *
 * @module nexabase-file
 */
@Tag(name = "文件管理", description = "文件上传、下载与删除等操作")
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileRecordController {

    private final FileRecordService fileRecordService;

    /**
     * 上传文件至对象存储。
     * <p>
     * 支持 MD5 秒传：同一租户内相同文件会复用物理存储，但创建独立记录。
     *
     * @param file 上传的文件对象
     * @return 文件记录信息
     */
    @Operation(summary = "上传文件", description = "上传文件至对象存储并记录文件信息，支持秒传")
    @PostMapping("/upload")
    public Result<FileRecord> upload(
            @Parameter(description = "上传的文件对象", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            Long userId = UserContext.getCurrentUserId();
            if (userId == null) {
                userId = 1L; // 兜底：未认证场景
            }
            Long tenantId = 1L; // TODO: 从用户上下文或 JWT 获取租户ID

            FileRecord record = fileRecordService.upload(file, tenantId, userId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件。
     * <p>
     * 返回文件流，浏览器将根据 Content-Disposition 自动触发下载。
     *
     * @param id 文件记录 ID
     * @return 文件流响应
     */
    @Operation(summary = "下载文件", description = "根据ID下载文件，支持流式传输")
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(
            @Parameter(description = "文件记录ID", required = true)
            @PathVariable Long id) {
        try {
            InputStream inputStream = fileRecordService.download(id);
            FileRecord record = fileRecordService.getById(id);

            // URL 编码文件名，防止中文乱码
            String encodedFileName = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.parseMediaType(record.getContentType()))
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 逻辑删除文件。
     * <p>
     * 仅标记记录为已删除，物理文件由后台定时任务清理。
     *
     * @param id 文件记录 ID
     * @return 成功响应
     */
    @Operation(summary = "删除文件", description = "逻辑删除指定ID的文件记录，后台异步物理清理")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "文件记录ID", required = true)
            @PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            userId = 1L; // 兜底
        }
        fileRecordService.deleteFile(id, userId);
        return Result.success();
    }
}
