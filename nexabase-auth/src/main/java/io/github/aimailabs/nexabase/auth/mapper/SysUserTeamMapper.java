package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysUserTeam;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-团队关联 Mapper。
 */
@Mapper
public interface SysUserTeamMapper extends BaseMapper<SysUserTeam> {

    @Delete("DELETE FROM sys_user_team WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM sys_user_team WHERE team_id = #{teamId}")
    int deleteByTeamId(@Param("teamId") Long teamId);

    @Insert("<script>" +
            "INSERT INTO sys_user_team (user_id, team_id, is_leader) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.userId}, #{item.teamId}, #{item.isLeader})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("list") List<SysUserTeam> list);
}
