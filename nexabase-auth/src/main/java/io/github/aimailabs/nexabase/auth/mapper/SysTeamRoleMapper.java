package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysTeamRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队-角色关联 Mapper（权限继承核心）。
 */
@Mapper
public interface SysTeamRoleMapper extends BaseMapper<SysTeamRole> {

    @Delete("DELETE FROM sys_team_role WHERE team_id = #{teamId}")
    int deleteByTeamId(@Param("teamId") Long teamId);

    @Delete("DELETE FROM sys_team_role WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT team_id FROM sys_team_role WHERE role_id = #{roleId}")
    List<Long> selectTeamIdsByRoleId(@Param("roleId") Long roleId);

    @Insert("<script>" +
            "INSERT INTO sys_team_role (team_id, role_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.teamId}, #{item.roleId})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<SysTeamRole> list);
}
