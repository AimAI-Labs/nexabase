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
}
