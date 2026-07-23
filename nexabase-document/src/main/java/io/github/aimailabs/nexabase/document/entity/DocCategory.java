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
 * 文档目录实体。
 * <p>
 * 采用物化路径（{@code path}）存储层级结构，格式如 {@code /1/4/9/}，
 * 避免递归查询，提升查询性能。
 */
@Data
@TableName("doc_category")
public class DocCategory {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属知识库ID */
    private Long kbId;

    /** 租户ID */
    private Long tenantId;

    /** 父节点ID，0 表示根节点 */
    private Long parentId;

    /** 物化路径，格式如 /1/4/9/ */
    private String path;

    /** 目录名称 */
    @NotBlank(message = "目录名称不能为空")
    private String name;

    /** 排序权重 */
    private Integer sortOrder;

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
