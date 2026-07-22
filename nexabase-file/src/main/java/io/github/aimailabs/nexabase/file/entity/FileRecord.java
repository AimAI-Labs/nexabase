package io.github.aimailabs.nexabase.file.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件存储记录实体。
 * <p>
 * 记录上传到对象存储（S3/MinIO/RustFS）的文件元数据信息，
 * 支持基于 MD5 的秒传功能和多租户隔离。
 */
@Data
@TableName("file_record")
public class FileRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 文件 MD5 校验值，用于秒传检测 */
    private String md5;

    /** 文件大小（字节） */
    private Long size;

    /** MIME 类型 */
    private String contentType;

    /** 对象存储 Bucket 名称 */
    private String bucket;

    /** 对象存储路径 */
    private String objectPath;

    /** 租户ID/团队ID（多租户隔离） */
    private Long tenantId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 创建人ID（上传人） */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 更新人ID */
    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    /** 逻辑删除标记：0-未删除，1-已删除 */
    private Integer isDeleted;
}
