package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.github.aimailabs.nexabase.auth.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称 */
    private String name;

    /** 角色标识(如 SUPER_ADMIN) */
    private String code;

    /** 角色描述 */
    private String description;

    /** 状态: 1-正常, 0-停用 */
    private Integer status;
}
