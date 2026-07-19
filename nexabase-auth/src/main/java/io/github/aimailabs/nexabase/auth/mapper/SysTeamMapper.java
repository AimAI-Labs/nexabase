package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 团队 Mapper。
 */
@Mapper
public interface SysTeamMapper extends BaseMapper<SysTeam> {

    /**
     * 查询直属子团队。
     */
    @Select("SELECT * FROM sys_team WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order")
    List<SysTeam> selectChildren(@Param("parentId") Long parentId);

    /**
     * 查询团队下所有用户 ID。
     */
    @Select("SELECT user_id FROM sys_user_team WHERE team_id = #{teamId}")
    List<Long> selectUserIdsByTeamId(@Param("teamId") Long teamId);
}
