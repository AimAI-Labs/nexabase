package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的角色 code 集合（直接角色 ∪ 团队继承角色）。
     */
    @Select("SELECT DISTINCT r.code FROM sys_role r " +
            "WHERE r.id IN (" +
            "  SELECT role_id FROM sys_user_role WHERE user_id = #{userId} " +
            "  UNION " +
            "  SELECT role_id FROM sys_team_role WHERE team_id IN " +
            "  (SELECT team_id FROM sys_user_team WHERE user_id = #{userId})" +
            ") AND r.is_deleted = 0 AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
