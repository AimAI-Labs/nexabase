package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysUserTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-团队关联 Mapper。
 */
@Mapper
public interface SysUserTeamMapper extends BaseMapper<SysUserTeam> {

    int insertBatch(@Param("list") List<SysUserTeam> list);
}
