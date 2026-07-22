package io.github.aimailabs.nexabase.document.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档基础信息实体。
 * <p>
 * 存储文档的元数据信息，正文内容存储在 MongoDB 的 {@code doc_content} 集合中。
 */
@Data
@TableName("doc_info")
public class DocInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属知识库ID */
    private Long kbId;

    /** 所属目录ID */
    private Long categoryId;

    /** 租户ID/团队ID */
    private Long tenantId;

    /** 文档标题 */
    private String title;

    /** 作者ID */
    private Long authorId;

    /** 文档状态：0-草稿，1-已发布 */
    private Integer status;

    /** 关联的附件文件ID（可选，用于支持上传附件场景） */
    private Long fileId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(fill = FieldFill.UPDATE)
    private Long updatedBy;

    private Integer isDeleted;
}
