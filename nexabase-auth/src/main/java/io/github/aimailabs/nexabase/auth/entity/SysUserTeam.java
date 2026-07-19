package io.github.aimailabs.nexabase.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户-团队关联实体（联合主键）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_team")
public class SysUserTeam {

    private Long userId;

    private Long teamId;

    /** 是否为团队负责人: 1-是, 0-否 */
    private Integer isLeader;
}
