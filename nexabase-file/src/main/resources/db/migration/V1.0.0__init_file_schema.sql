CREATE TABLE `file_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `md5` varchar(64) NOT NULL COMMENT '文件MD5值',
  `size` bigint NOT NULL COMMENT '文件大小(字节)',
  `content_type` varchar(100) NOT NULL COMMENT 'MIME类型',
  `bucket` varchar(100) NOT NULL COMMENT '存储Bucket名',
  `object_path` varchar(255) NOT NULL COMMENT 'Object路径',
  `tenant_id` bigint NOT NULL COMMENT '租户ID/团队ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint NOT NULL COMMENT '创建人ID(即上传人ID)',
  `updated_by` bigint COMMENT '更新人ID',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_md5` (`md5`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储记录表';
