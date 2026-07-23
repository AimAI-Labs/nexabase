package io.github.aimailabs.nexabase.document.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体。
 * <p>
 * 知识库是文档的顶层容器，用于组织和管理一组相关文档。
 */
@Data
@TableName("doc_knowledge_base")
public class DocKnowledgeBase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 知识库名称 */
    @NotBlank(message = "知识库名称不能为空")
    private String name;

    /** 知识库描述 */
    private String description;

    /** 租户ID/团队ID */
    private Long tenantId;

    /** 所有者ID */
    private Long ownerId;

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
