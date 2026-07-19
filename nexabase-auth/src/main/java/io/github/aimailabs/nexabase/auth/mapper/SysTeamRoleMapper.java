package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysTeamRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队-角色关联 Mapper（权限继承核心）。
 */
@Mapper
public interface SysTeamRoleMapper extends BaseMapper<SysTeamRole> {

    int insertBatch(@Param("list") List<SysTeamRole> list);
}
