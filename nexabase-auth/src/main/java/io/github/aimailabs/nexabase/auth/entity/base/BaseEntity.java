package io.github.aimailabs.nexabase.auth.entity.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类。
 * <p>
 * 统一主键策略与审计字段：
 * <ul>
 *   <li>主键：雪花算法（{@link IdType#ASSIGN_ID}），BIGINT</li>
 *   <li>审计字段：{@code createdAt}/{@code updatedAt}/{@code createdBy}/{@code updatedBy}，
 *       由 {@code MetaObjectHandler} 自动填充</li>
 *   <li>逻辑删除：{@code is_deleted}，{@link TableLogic} 自动过滤已删除记录</li>
 * </ul>
 */
@Data
public abstract class BaseEntity {

    /** 主键 ID（雪花算法生成） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 创建人 ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 更新人 ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /** 逻辑删除标识：0-未删除, 1-已删除 */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}
