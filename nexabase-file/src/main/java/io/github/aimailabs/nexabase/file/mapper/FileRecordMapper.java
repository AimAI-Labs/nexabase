package io.github.aimailabs.nexabase.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.file.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
}
