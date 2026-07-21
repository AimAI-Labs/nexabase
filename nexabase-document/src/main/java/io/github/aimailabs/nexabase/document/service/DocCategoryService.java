package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocCategory;

public interface DocCategoryService {
    DocCategory create(DocCategory category);
    DocCategory getById(Long id);
    void delete(Long id, Long userId);
}
