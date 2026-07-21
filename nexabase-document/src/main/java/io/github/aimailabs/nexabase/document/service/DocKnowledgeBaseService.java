package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocKnowledgeBase;

public interface DocKnowledgeBaseService {
    DocKnowledgeBase create(DocKnowledgeBase kb);
    DocKnowledgeBase getById(Long id);
    void delete(Long id, Long userId);
}
