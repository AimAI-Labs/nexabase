package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocInfo;

/**
 * 文档服务接口。
 */
public interface DocInfoService {

    /**
     * 创建文档。
     *
     * @param docInfo 文档元数据
     * @param content 文档正文
     * @return 创建的文档实体
     */
    DocInfo create(DocInfo docInfo, String content);

    /**
     * 更新文档。
     *
     * @param id      文档ID
     * @param docInfo 文档元数据
     * @param content 文档正文
     * @param userId  操作人ID
     * @return 更新后的文档实体
     */
    DocInfo update(Long id, DocInfo docInfo, String content, Long userId);

    /**
     * 根据ID查询文档。
     *
     * @param id 文档ID
     * @return 文档实体
     */
    DocInfo getById(Long id);

    /**
     * 删除文档。
     *
     * @param id     文档ID
     * @param userId 操作人ID
     */
    void delete(Long id, Long userId);

    /**
     * 通过关联的附件文件创建文档（file↔document 联合链路）。
     * <p>根据 fileId 经 Feign 调用 file 模块拉取文件解析后的纯文本，
     * 写入 MongoDB doc_content 正文，并投递 CREATE 事件触发向量化。
     *
     * @param docInfo 文档元数据（kbId / categoryId / title 等，fileId 为必填）
     * @param userId  操作人ID
     * @return 创建成功的文档元数据
     */
    DocInfo createFromFile(DocInfo docInfo, Long userId);
}
