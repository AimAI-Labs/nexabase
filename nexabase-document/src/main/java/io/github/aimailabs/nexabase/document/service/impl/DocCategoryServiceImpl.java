package io.github.aimailabs.nexabase.document.service.impl;

import io.github.aimailabs.nexabase.document.entity.DocCategory;
import io.github.aimailabs.nexabase.document.mapper.DocCategoryMapper;
import io.github.aimailabs.nexabase.document.service.DocCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocCategoryServiceImpl implements DocCategoryService {
    
    private final DocCategoryMapper mapper;

    @Override
    public DocCategory create(DocCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getPath() == null) {
            category.setPath("/");
        }
        mapper.insert(category);
        return category;
    }

    @Override
    public DocCategory getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void delete(Long id, Long userId) {
        DocCategory category = mapper.selectById(id);
        if (category != null) {
            category.setIsDeleted(1);
            category.setUpdatedBy(userId);
            mapper.updateById(category);
        }
    }
}
