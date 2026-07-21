package io.github.aimailabs.nexabase.document.service.impl;

import io.github.aimailabs.nexabase.document.entity.DocKnowledgeBase;
import io.github.aimailabs.nexabase.document.mapper.DocKnowledgeBaseMapper;
import io.github.aimailabs.nexabase.document.service.DocKnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocKnowledgeBaseServiceImpl implements DocKnowledgeBaseService {
    
    private final DocKnowledgeBaseMapper mapper;

    @Override
    public DocKnowledgeBase create(DocKnowledgeBase kb) {
        mapper.insert(kb);
        return kb;
    }

    @Override
    public DocKnowledgeBase getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void delete(Long id, Long userId) {
        DocKnowledgeBase kb = mapper.selectById(id);
        if (kb != null) {
            kb.setIsDeleted(1);
            kb.setUpdatedBy(userId);
            mapper.updateById(kb);
        }
    }
}
