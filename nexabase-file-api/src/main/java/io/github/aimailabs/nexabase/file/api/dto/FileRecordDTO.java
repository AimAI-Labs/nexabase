package io.github.aimailabs.nexabase.file.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件记录 DTO。
 * <p>
 * 用于服务间传递文件元数据信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecordDTO implements Serializable {

    /** 文件记录ID */
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 文件 MD5 */
    private String md5;

    /** 文件大小（字节） */
    private Long size;

    /** MIME 类型 */
    private String contentType;

    /** 存储 Bucket */
    private String bucket;

    /** 对象存储路径 */
    private String objectPath;

    /** 下载 URL（可选，由调用方或前端拼接） */
    private String downloadUrl;

    /** 租户ID */
    private Long tenantId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 创建人ID */
    private Long createdBy;
}
