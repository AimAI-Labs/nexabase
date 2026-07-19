package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-权限关联 Mapper。
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    int insertBatch(@Param("list") List<SysRolePermission> list);
}
