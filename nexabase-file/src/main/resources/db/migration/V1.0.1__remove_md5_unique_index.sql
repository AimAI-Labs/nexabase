-- 移除 md5 字段的唯一索引，支持秒传时为每个上传创建独立记录
-- 同一 MD5 的文件可以有多条记录（不同 tenant/user），但复用同一个 objectPath

ALTER TABLE `file_record` 
DROP INDEX `uk_md5`;

-- 添加联合索引以优化秒传查询性能（按 tenant_id + md5 查询）
ALTER TABLE `file_record` 
ADD INDEX `idx_tenant_md5` (`tenant_id`, `md5`, `is_deleted`);
