CREATE TABLE `doc_knowledge_base` (
  `id` bigint NOT NULL COMMENT '知识库ID',
  `name` varchar(100) NOT NULL COMMENT '知识库名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `tenant_id` bigint NOT NULL COMMENT '租户ID/团队ID',
  `owner_id` bigint NOT NULL COMMENT '所属人/团队ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint,
  `updated_by` bigint,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

CREATE TABLE `doc_category` (
  `id` bigint NOT NULL COMMENT '目录ID',
  `kb_id` bigint NOT NULL COMMENT '所属知识库ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID/团队ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父节点ID',
  `path` varchar(255) NOT NULL COMMENT '层级路径, 格式如 /1/4/9/',
  `name` varchar(100) NOT NULL COMMENT '目录名称',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint,
  `updated_by` bigint,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_kb_id` (`kb_id`),
  KEY `idx_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档目录树表';

CREATE TABLE `doc_info` (
  `id` bigint NOT NULL COMMENT '文档ID',
  `kb_id` bigint NOT NULL COMMENT '所属知识库ID',
  `category_id` bigint NOT NULL COMMENT '所属目录ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID/团队ID',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `author_id` bigint NOT NULL COMMENT '作者ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿, 1-已发布',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint,
  `updated_by` bigint,
  `is_deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_kb_cat` (`kb_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文档基础信息表';
