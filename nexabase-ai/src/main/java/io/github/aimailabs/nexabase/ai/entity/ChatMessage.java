package io.github.aimailabs.nexabase.ai.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 对话消息明细实体。
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息业务唯一 UUID
     */
    private String messageId;

    /**
     * 关联会话 UUID
     */
    private String sessionId;

    /**
     * 提问用户 ID
     */
    private Long userId;

    /**
     * 角色: user / assistant / system
     */
    private String role;

    /**
     * 消息文本正文
     */
    private String content;

    /**
     * 意图改写后的独立检索 Query
     */
    private String rewriteQuery;

    /**
     * 引用的知识库切片快照 (JSON 字符串)
     */
    private String citations;

    /**
     * 输入 Token 消耗
     */
    private Integer inputTokens;

    /**
     * 输出 Token 消耗
     */
    private Integer outputTokens;

    /**
     * 多路检索与融合耗时 (毫秒)
     */
    private Integer retrieveCostMs;

    /**
     * 大模型生成耗时 (毫秒)
     */
    private Integer llmCostMs;

    /**
     * 总端到端耗时 (毫秒)
     */
    private Integer totalCostMs;

    /**
     * 用户反馈 (0: 无反馈, 1: 点赞, 2: 点踩)
     */
    private Integer feedbackStatus;

    /**
     * 用户反馈备注/原因
     */
    private String feedbackRemark;

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
}
