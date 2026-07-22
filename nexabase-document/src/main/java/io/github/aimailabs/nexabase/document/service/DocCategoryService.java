package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocCategory;

import java.util.List;

/**
 * 目录服务接口。
 */
public interface DocCategoryService {

    /**
     * 创建目录（自动计算物化路径）。
     *
     * @param category 目录实体
     * @return 创建后的目录（含计算好的 path）
     */
    DocCategory create(DocCategory category);

    /**
     * 根据ID查询目录。
     *
     * @param id 目录ID
     * @return 目录实体
     */
    DocCategory getById(Long id);

    /**
     * 查询指定知识库的全部目录列表（不含已删除）。
     *
     * @param kbId 知识库ID
     * @return 目录列表
     */
    List<DocCategory> listByKb(Long kbId);

    /**
     * 逻辑删除目录。
     *
     * @param id     目录ID
     * @param userId 操作人ID
     */
    void delete(Long id, Long userId);
}
