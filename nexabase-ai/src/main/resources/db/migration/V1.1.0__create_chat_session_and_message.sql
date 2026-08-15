-- AI 会话与消息持久化表结构
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话业务唯一UUID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `title` VARCHAR(128) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    `kb_id` BIGINT DEFAULT NULL COMMENT '绑定的知识库ID(NULL表示全局/通用问答)',
    `model_name` VARCHAR(64) NOT NULL DEFAULT 'qwen-plus' COMMENT '绑定的模型名称',
    `system_prompt` TEXT DEFAULT NULL COMMENT '自定义System Prompt(覆盖默认提示词)',
    `temperature` DECIMAL(3,2) NOT NULL DEFAULT 0.70 COMMENT '采样温度(0.0~1.0)',
    `is_pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶(0:否, 1:是)',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:未删除, 1:已删除)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_updated` (`user_id`, `is_deleted`, `updated_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话会话表';

CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `message_id` VARCHAR(64) NOT NULL COMMENT '消息业务唯一UUID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '关联会话UUID',
    `user_id` BIGINT NOT NULL COMMENT '提问用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user / assistant / system',
    `content` LONGTEXT NOT NULL COMMENT '消息文本正文',
    `rewrite_query` VARCHAR(512) DEFAULT NULL COMMENT '意图改写后的独立检索Query',
    `citations` JSON DEFAULT NULL COMMENT '引用的知识库切片快照(JSON数组)',
    `input_tokens` INT DEFAULT 0 COMMENT '输入Token消耗',
    `output_tokens` INT DEFAULT 0 COMMENT '输出Token消耗',
    `retrieve_cost_ms` INT DEFAULT 0 COMMENT '多路检索与融合耗时(毫秒)',
    `llm_cost_ms` INT DEFAULT 0 COMMENT '大模型生成耗时(毫秒)',
    `total_cost_ms` INT DEFAULT 0 COMMENT '总端到端耗时(毫秒)',
    `feedback_status` TINYINT DEFAULT 0 COMMENT '用户反馈(0:无反馈, 1:点赞, 2:点踩)',
    `feedback_remark` VARCHAR(255) DEFAULT NULL COMMENT '用户反馈备注/原因',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:未删除, 1:已删除)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_session_created` (`session_id`, `is_deleted`, `created_at` ASC),
    KEY `idx_user_created` (`user_id`, `created_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话消息明细表';
