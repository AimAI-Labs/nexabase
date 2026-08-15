package io.github.aimailabs.nexabase.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.aimailabs.nexabase.ai.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息 Mapper。
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
