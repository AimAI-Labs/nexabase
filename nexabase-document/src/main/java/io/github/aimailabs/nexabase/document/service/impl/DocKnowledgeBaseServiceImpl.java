package io.github.aimailabs.nexabase.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.document.entity.DocKnowledgeBase;
import io.github.aimailabs.nexabase.document.mapper.DocKnowledgeBaseMapper;
import io.github.aimailabs.nexabase.document.service.DocKnowledgeBaseService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocKnowledgeBaseServiceImpl implements DocKnowledgeBaseService {
    
    private final DocKnowledgeBaseMapper mapper;

    @Override
    public DocKnowledgeBase create(DocKnowledgeBase kb) {
        // 同名知识库不允许重复（排除已逻辑删除的记录）
        boolean nameExists = mapper.exists(new LambdaQueryWrapper<DocKnowledgeBase>()
                .eq(DocKnowledgeBase::getName, kb.getName())
                .eq(DocKnowledgeBase::getIsDeleted, 0));
        if (nameExists) {
            throw new BusinessException(ResultCode.CONFLICT, "知识库名称「" + kb.getName() + "」已存在，请更换名称");
        }
        if (kb.getTenantId() == null) {
            kb.setTenantId(1L);
        }
        mapper.insert(kb);
        return kb;
    }

    @Override
    public DocKnowledgeBase getById(Long id) {
        DocKnowledgeBase kb = mapper.selectById(id);
        if (kb == null || kb.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "知识库不存在或已被删除");
        }
        return kb;
    }

    @Override
    public void delete(Long id, Long userId) {
        DocKnowledgeBase kb = mapper.selectById(id);
        if (kb == null || kb.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.CONFLICT, "知识库不存在或已被删除，无需重复操作");
        }
        kb.setIsDeleted(1);
        kb.setUpdatedBy(userId);
        mapper.updateById(kb);
    }
}
