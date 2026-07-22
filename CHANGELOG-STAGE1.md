# Nexabase 第一阶段开发日志

## [v0.2.0] - 2026-07-22

### ✨ 新增功能

#### nexabase-file（文件服务）
- **文件上传**：支持 S3 兼容对象存储（RustFS/MinIO/OSS）
- **文件下载**：流式传输，自动处理中文文件名 URL 编码
- **文件秒传**：基于 MD5 检测，同租户内复用物理存储但创建独立记录
- **租户隔离**：文件记录按 `tenant_id` 隔离
- **内部 API**：`FileInternalController` 提供 Feign 调用接口
- **Feign Client**：`FileClient` 供其他微服务查询文件元数据

#### nexabase-document（文档服务）
- **知识库管理**：创建、查询、删除知识库
- **目录管理**：
  - 无限层级树形结构
  - 物化路径自动计算（格式：`/parentId1/parentId2/selfId/`）
  - 同级目录名唯一性校验
  - 删除前检查子目录和文档依赖
- **文档管理**：
  - 创建文档：MySQL（元数据）+ MongoDB（正文）+ MQ 事件
  - **更新文档**：支持标题、状态、附件关联、正文更新 + MQ 事件
  - 查询文档：根据 ID 查询元数据
  - 删除文档：逻辑删除 MySQL + 物理删除 MongoDB + MQ 事件
- **目录树查询**：`GET /api/v1/document/category/tree/{kbId}` 获取完整目录列表
- **事件驱动**：文档变更自动投递到 RabbitMQ（CREATE / UPDATE / DELETE）

#### RabbitMQ 配置
- **Exchange**：`document.exchange`（Topic Exchange）
- **Queue**：
  - `document.queue.search` — 供 nexabase-search 消费（ES 索引同步）
  - `document.queue.ai` — 供 nexabase-ai 消费（向量化处理）
- **死信队列**：`document.dlq` — 防止消息丢失
- **消息转换器**：Jackson2JsonMessageConverter（跨服务兼容）

---

### 🔧 优化改进

#### 代码质量
- **用户上下文统一**：所有 Controller 统一使用 `UserContext.getCurrentUserId()`，移除手动 `request.getHeader("X-User-Id")` 逻辑
- **异常处理统一**：使用 `BusinessException(ResultCode, message)` 替代原始 `RuntimeException`
- **审计字段自动填充**：
  - 所有实体类添加 `@TableField(fill = FieldFill.XXX)` 注解
  - 创建 `MybatisPlusConfig implements MetaObjectHandler`

#### 实体类增强
- **FileRecord**：添加 `createdAt` / `updatedAt` 字段及自动填充注解
- **DocInfo**：添加 `fileId` 字段（支持文档关联附件）
- **DocCategory**：完善物化路径计算逻辑
- **DocKnowledgeBase**：标准化审计字段

#### API 接口完善
- **file-api**：
  - `FileRecordDTO` 增强：补充 md5、size、contentType、downloadUrl、createdAt 等字段
  - `FileClient` Feign 接口：提供 `getFileById` 方法
- **document-api**：保持现有结构

---

### 🐛 Bug 修复

1. **秒传跨租户隔离问题**
   - **问题**：不同租户上传同一文件会共用同一条 DB 记录
   - **解决**：秒传检测加入 `tenant_id` 过滤，复用 `objectPath` 但创建新记录

2. **目录物化路径未计算**
   - **问题**：路径字段被硬编码为 `"/"`
   - **解决**：实现 `buildPath()` 方法，自动计算层级路径

3. **文档更新接口缺失**
   - **问题**：原服务仅支持 CREATE / READ / DELETE
   - **解决**：新增 `update(Long id, DocInfo docInfo, String content, Long userId)` 方法

4. **异常提示不友好**
   - **问题**：抛出 `RuntimeException("File not found")`
   - **解决**：改为 `BusinessException(ResultCode.RESOURCE_NOT_FOUND, "文件不存在或已被删除")`

5. **file-api 编译失败**
   - **问题**：缺少 OpenFeign 和 Lombok 依赖
   - **解决**：pom.xml 添加 `spring-cloud-starter-openfeign` 和 `lombok` 依赖

---

### 📁 新增文件

```
nexabase-file/
├── src/main/java/.../file/
│   ├── config/
│   │   └── MybatisPlusConfig.java                 # MyBatis-Plus 自动填充
│   └── controller/
│       └── FileInternalController.java            # Feign 内部接口

nexabase-file-api/
└── src/main/java/.../file/api/
    ├── client/
    │   └── FileClient.java                        # Feign Client
    └── dto/
        └── FileRecordDTO.java (增强)              # 完整字段

nexabase-document/
├── src/main/java/.../document/
│   └── config/
│       ├── MybatisPlusConfig.java                 # MyBatis-Plus 自动填充
│       └── RabbitMqConfig.java                    # RabbitMQ 配置
└── src/main/resources/db/migration/
    └── V1.0.1__add_file_id_to_doc_info.sql        # 添加 file_id 字段

docs/
└── stage1-implementation-summary.md               # 第一阶段总结文档
```

---

### 📊 代码统计

| 模块 | 新增文件 | 修改文件 | 总代码行数（估算） |
|---|---|---|---|
| nexabase-file | 2 | 4 | ~450 行 |
| nexabase-file-api | 1 | 2 | ~60 行 |
| nexabase-document | 3 | 6 | ~700 行 |
| 文档 | 2 | 0 | ~900 行 |
| **合计** | **8** | **12** | **~2110 行** |

---

### ✅ 编译验证

```bash
mvn clean install -DskipTests -pl nexabase-file-api,nexabase-file,nexabase-document -am
```

**结果**：✅ BUILD SUCCESS（13.226s）

---

### 🎯 下一步工作

1. **nexabase-search**（全文检索服务）⭐⭐⭐⭐
   - MQ 消费者监听 `document.queue.search`
   - Elasticsearch 索引管理
   - BM25 关键词召回接口

2. **nexabase-ai**（AI 与向量化）⭐⭐⭐⭐⭐
   - MQ 消费者监听 `document.queue.ai`
   - LangChain4j 文档分块
   - Embedding 模型调用
   - Qdrant 向量存储
   - RAG 混合检索（BM25 + Vector + RRF）

3. **文件物理清理**
   - 定时任务清理已删除文件
   - S3 `deleteObject` 物理清理

4. **文档分页查询**
   - 列表分页
   - 高级搜索（标题/作者/状态）

---

### 🔗 相关文档

- [第一阶段实现总结](./docs/stage1-implementation-summary.md)
- [项目总览](./docs/project-overview.md)
- [后续开发计划](./docs/future-development-plan.md)
- [API 文档 - Knife4j](http://localhost:8003/doc.html)

---

**变更提交人**: Kiro AI Assistant  
**代码审核**: [待补充]  
**测试状态**: ⚠️ 单元测试待补充  
**部署状态**: 🚧 开发环境
