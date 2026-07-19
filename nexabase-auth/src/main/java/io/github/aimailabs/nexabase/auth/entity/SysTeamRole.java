package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 团队-角色关联实体（联合主键，权限继承核心）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_team_role")
public class SysTeamRole {

    private Long teamId;

    private Long roleId;
}
