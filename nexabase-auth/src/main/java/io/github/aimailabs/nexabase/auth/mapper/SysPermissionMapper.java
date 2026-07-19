package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限资源 Mapper。
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询全部权限（按排序），Service 层内存组装父子树结构。
     */
    @Select("SELECT * FROM sys_permission WHERE is_deleted = 0 ORDER BY sort_order")
    List<SysPermission> selectAllOrdered();

    /**
     * 查询指定角色集合拥有的权限。
     */
    @Select("<script>" +
            "SELECT DISTINCT p.* FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "WHERE p.is_deleted = 0 AND rp.role_id IN " +
            "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<SysPermission> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}
