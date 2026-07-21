package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocInfo;

public interface DocInfoService {
    DocInfo create(DocInfo docInfo, String content);
    DocInfo getById(Long id);
    void delete(Long id, Long userId);
}
