package io.github.aimailabs.nexabase.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.document.entity.DocCategory;
import io.github.aimailabs.nexabase.document.mapper.DocCategoryMapper;
import io.github.aimailabs.nexabase.document.mapper.DocInfoMapper;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.service.DocCategoryService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 目录服务实现类。
 * <p>
 * 核心功能：
 * <ul>
 *   <li>创建目录时自动计算物化路径 {@code /parentId1/parentId2/newId/}</li>
 *   <li>防止同级目录名重复</li>
 *   <li>删除时检查目录下是否存在文档</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocCategoryServiceImpl implements DocCategoryService {

    private final DocCategoryMapper mapper;
    private final DocInfoMapper docInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocCategory create(DocCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }

        // 同一知识库、同一父节点下，目录名不允许重复
        boolean nameExists = mapper.exists(new LambdaQueryWrapper<DocCategory>()
                .eq(DocCategory::getKbId, category.getKbId())
                .eq(DocCategory::getParentId, category.getParentId())
                .eq(DocCategory::getName, category.getName())
                .eq(DocCategory::getIsDeleted, 0));
        if (nameExists) {
            throw new BusinessException(ResultCode.CONFLICT, "同级目录下名称「" + category.getName() + "」已存在");
        }

        // 方案：先设置临时路径 "/" 以通过数据库 NOT NULL 校验
        // 插入后立即更新为正确的物化路径
        category.setPath("/");
        mapper.insert(category);

        // 计算物化路径：父路径 + 自身ID + /
        String path = buildPath(category.getParentId(), category.getId());
        category.setPath(path);
        mapper.updateById(category);

        log.info("目录创建成功，categoryId={}, path={}", category.getId(), path);
        return category;
    }

    @Override
    public DocCategory getById(Long id) {
        DocCategory category = mapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "目录不存在或已被删除");
        }
        return category;
    }

    @Override
    public List<DocCategory> listByKb(Long kbId) {
        return mapper.selectList(new LambdaQueryWrapper<DocCategory>()
                .eq(DocCategory::getKbId, kbId)
                .eq(DocCategory::getIsDeleted, 0)
                .orderByAsc(DocCategory::getParentId)
                .orderByAsc(DocCategory::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        DocCategory category = mapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.CONFLICT, "目录不存在或已被删除，无需重复操作");
        }

        // 检查目录下是否存在有效文档
        boolean hasDoc = docInfoMapper.exists(new LambdaQueryWrapper<DocInfo>()
                .eq(DocInfo::getCategoryId, id)
                .eq(DocInfo::getIsDeleted, 0));
        if (hasDoc) {
            throw new BusinessException(ResultCode.CONFLICT, "目录下存在文档，请先移除或删除文档后再操作");
        }

        // 检查是否有子目录
        boolean hasChildren = mapper.exists(new LambdaQueryWrapper<DocCategory>()
                .eq(DocCategory::getParentId, id)
                .eq(DocCategory::getIsDeleted, 0));
        if (hasChildren) {
            throw new BusinessException(ResultCode.CONFLICT, "目录下存在子目录，请先删除子目录后再操作");
        }

        category.setIsDeleted(1);
        category.setUpdatedBy(userId);
        mapper.updateById(category);
    }

    /**
     * 计算物化路径。
     * <p>
     * 根节点（parentId=0）的路径为 {@code /id/}，
     * 子节点路径为父节点路径 + 自身ID + {@code /}。
     *
     * @param parentId 父节点ID
     * @param selfId   自身ID
     * @return 物化路径，如 {@code /1/4/9/}
     */
    private String buildPath(Long parentId, Long selfId) {
        if (parentId == null || parentId == 0L) {
            return "/" + selfId + "/";
        }
        DocCategory parent = mapper.selectById(parentId);
        if (parent == null || parent.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "父目录不存在");
        }
        return parent.getPath() + selfId + "/";
    }
}
