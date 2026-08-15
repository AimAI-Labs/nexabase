package io.github.aimailabs.nexabase.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 对话会话实体。
 */
@Data
@TableName("chat_session")
public class ChatSession {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话业务唯一 UUID
     */
    private String sessionId;

    /**
     * 所属用户 ID
     */
    private Long userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 绑定的知识库 ID (null 表示全局问答)
     */
    private Long kbId;

    /**
     * 绑定的模型名称
     */
    private String modelName;

    /**
     * 自定义 System Prompt
     */
    private String systemPrompt;

    /**
     * 采样温度 (0.0~1.0)
     */
    private BigDecimal temperature;

    /**
     * 是否置顶 (0: 否, 1: 是)
     */
    private Integer isPinned;

    /**
     * 逻辑删除 (0: 未删除, 1: 已删除)
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
