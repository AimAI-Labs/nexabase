package io.github.aimailabs.nexabase.file.controller;

import io.github.aimailabs.nexabase.file.api.dto.FileRecordDTO;
import io.github.aimailabs.nexabase.file.entity.FileRecord;
import io.github.aimailabs.nexabase.file.service.FileRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件服务 / 文件内部接口
 * <p>
 * 此接口不经过网关鉴权，仅供微服务间调用，返回原始数据（不包装 Result）。
 *
 * @module nexabase-file
 */
@RestController
@RequestMapping("/api/v1/file/internal")
@RequiredArgsConstructor
public class FileInternalController {

    private final FileRecordService fileRecordService;

    /**
     * 根据 ID 查询文件元数据（内部接口）。
     *
     * @param id 文件 ID
     * @return 文件元数据 DTO
     */
    @GetMapping("/{id}")
    public FileRecordDTO getFileById(@PathVariable Long id) {
        FileRecord record = fileRecordService.getById(id);
        
        // 手动映射字段（因 fileId-api 独立，不依赖 Spring）
        return FileRecordDTO.builder()
                .id(record.getId())
                .fileName(record.getFileName())
                .md5(record.getMd5())
                .size(record.getSize())
                .contentType(record.getContentType())
                .bucket(record.getBucket())
                .objectPath(record.getObjectPath())
                .downloadUrl("/api/v1/file/download/" + record.getId())
                .tenantId(record.getTenantId())
                .createdAt(record.getCreatedAt())
                .createdBy(record.getCreatedBy())
                .build();
    }
}
