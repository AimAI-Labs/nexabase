package io.github.aimailabs.nexabase.auth.mapper;

import io.github.aimailabs.nexabase.auth.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper。
 * <p>
 * 提供登录查询与权限聚合核心查询。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户拥有的角色 ID 集合（直接角色 ∪ 团队继承角色）。
     */
    @Select("""
            SELECT role_id FROM sys_user_role WHERE user_id = #{userId}
            UNION ALL
            SELECT role_id FROM sys_team_role WHERE team_id IN
            (SELECT team_id FROM sys_user_team WHERE user_id = #{userId})
            """)
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 权限聚合核心查询：聚合直接角色 + 团队角色 → 角色-权限 → 权限标识。
     * <p>
     * 超管（SUPER_ADMIN）由 Service 层短路，不走此 SQL。
     */
    List<String> selectPermissionKeysByUserId(@Param("userId") Long userId);

    /**
     * 递增用户 JWT 版本号（用于批量踢下线）。
     */
    @org.apache.ibatis.annotations.Update(
            "UPDATE sys_user SET jwt_version = jwt_version + 1 WHERE id = #{userId} AND is_deleted = 0")
    int incrementJwtVersion(@Param("userId") Long userId);
}
