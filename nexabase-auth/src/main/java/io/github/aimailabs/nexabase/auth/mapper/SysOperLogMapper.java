package io.github.aimailabs.nexabase.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.auth.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
