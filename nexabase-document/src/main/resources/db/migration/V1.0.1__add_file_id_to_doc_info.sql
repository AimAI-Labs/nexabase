-- 为 doc_info 表添加 file_id 字段，支持文档关联附件文件
ALTER TABLE `doc_info`
ADD COLUMN `file_id` bigint NULL COMMENT '关联的附件文件ID（可选）' AFTER `status`,
ADD KEY `idx_file_id` (`file_id`);
