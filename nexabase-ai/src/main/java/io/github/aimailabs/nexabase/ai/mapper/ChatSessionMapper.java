package io.github.aimailabs.nexabase.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.ai.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper。
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
