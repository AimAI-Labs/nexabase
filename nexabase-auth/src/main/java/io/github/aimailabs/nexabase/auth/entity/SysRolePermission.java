package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色-权限关联实体（联合主键）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_permission")
public class SysRolePermission {

    private Long roleId;

    private Long permissionId;
}
