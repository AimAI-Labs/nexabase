package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocKnowledgeBase;

import java.util.List;

public interface DocKnowledgeBaseService {
    DocKnowledgeBase create(DocKnowledgeBase kb);
    DocKnowledgeBase getById(Long id);
    List<DocKnowledgeBase> getByName(String name);
    void delete(Long id, Long userId);
}
