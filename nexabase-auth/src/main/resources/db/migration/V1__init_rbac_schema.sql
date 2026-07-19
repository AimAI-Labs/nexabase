-- ============================================================
-- Nexabase RBAC 初始化表结构
-- 包含：用户、团队、角色、权限、关联表、操作日志
-- ============================================================

-- -------------------------------------------------------
-- 用户表
-- -------------------------------------------------------
CREATE TABLE `sys_user` (
  `id`            BIGINT       NOT NULL,
  `username`      VARCHAR(64)  NOT NULL COMMENT '登录账号',
  `password_hash` VARCHAR(128) NOT NULL COMMENT 'BCrypt 哈希密码',
  `nickname`      VARCHAR(64)           COMMENT '用户昵称',
  `email`         VARCHAR(128)          COMMENT '邮箱',
  `phone`         VARCHAR(20)           COMMENT '手机号',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用',
  `jwt_version`   BIGINT       NOT NULL DEFAULT 1 COMMENT 'JWT 版本号(用于批量让 Token 失效)',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by`    BIGINT                COMMENT '创建人',
  `updated_by`    BIGINT                COMMENT '更新人',
  `is_deleted`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------------------
-- 团队/组织表
-- -------------------------------------------------------
CREATE TABLE `sys_team` (
  `id`         BIGINT      NOT NULL,
  `parent_id`  BIGINT      NOT NULL DEFAULT 0 COMMENT '父团队 ID(0 为根节点)',
  `name`       VARCHAR(64) NOT NULL COMMENT '团队名称',
  `type`       TINYINT     NOT NULL DEFAULT 1 COMMENT '类型: 1-部门, 2-项目组',
  `sort_order` INT         NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT,
  `updated_by` BIGINT,
  `is_deleted` TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队/组织表';

-- -------------------------------------------------------
-- 用户-团队关联表
-- -------------------------------------------------------
CREATE TABLE `sys_user_team` (
  `user_id`   BIGINT  NOT NULL,
  `team_id`   BIGINT  NOT NULL,
  `is_leader` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为团队负责人: 1-是, 0-否',
  PRIMARY KEY (`user_id`, `team_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-团队关联表';

-- -------------------------------------------------------
-- 角色表
-- -------------------------------------------------------
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL,
  `name`        VARCHAR(64)  NOT NULL COMMENT '角色名称',
  `code`        VARCHAR(64)  NOT NULL COMMENT '角色标识',
  `description` VARCHAR(255)          COMMENT '角色描述',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-停用',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`  BIGINT,
  `updated_by`  BIGINT,
  `is_deleted`  TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -------------------------------------------------------
-- 权限资源表（树形结构）
-- -------------------------------------------------------
CREATE TABLE `sys_permission` (
  `id`             BIGINT       NOT NULL,
  `parent_id`      BIGINT       NOT NULL DEFAULT 0 COMMENT '父权限 ID(0 为根)',
  `name`           VARCHAR(64)  NOT NULL COMMENT '权限名称',
  `type`           TINYINT      NOT NULL COMMENT '类型: 1-菜单, 2-按钮, 3-API 接口',
  `permission_key` VARCHAR(128)          COMMENT '权限标识(如 sys:user:add)',
  `path`           VARCHAR(255)          COMMENT '路由路径或 API URL',
  `method`         VARCHAR(10)           COMMENT '请求方法(API 权限用)',
  `component`      VARCHAR(255)          COMMENT '前端组件路径(菜单权限用)',
  `icon`           VARCHAR(64)           COMMENT '菜单图标',
  `sort_order`     INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`     BIGINT,
  `updated_by`     BIGINT,
  `is_deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_permission_key` (`permission_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限资源表';

-- -------------------------------------------------------
-- 用户-角色关联表（直接拥有角色）
-- -------------------------------------------------------
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- -------------------------------------------------------
-- 团队-角色关联表（权限继承核心）
-- -------------------------------------------------------
CREATE TABLE `sys_team_role` (
  `team_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`team_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队-角色关联表';

-- -------------------------------------------------------
-- 角色-权限关联表
-- -------------------------------------------------------
CREATE TABLE `sys_role_permission` (
  `role_id`       BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- -------------------------------------------------------
-- 操作日志表
-- -------------------------------------------------------
CREATE TABLE `sys_oper_log` (
  `id`              BIGINT       NOT NULL,
  `user_id`         BIGINT                COMMENT '操作人 ID',
  `username`        VARCHAR(64)           COMMENT '操作人名称',
  `module`          VARCHAR(64)           COMMENT '业务模块',
  `action`          VARCHAR(128)          COMMENT '操作动作',
  `method`          VARCHAR(10)           COMMENT 'HTTP 请求方法',
  `url`             VARCHAR(255)          COMMENT '请求 URL',
  `ip_address`      VARCHAR(64)           COMMENT '操作 IP',
  `request_params`  TEXT                  COMMENT '请求参数(脱敏)',
  `response_result` TEXT                  COMMENT '响应结果(截断存放)',
  `status`          TINYINT               COMMENT '状态: 1-成功, 0-失败',
  `error_msg`       TEXT                  COMMENT '错误信息',
  `cost_time`       BIGINT                COMMENT '耗时(毫秒)',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
