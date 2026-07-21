package io.github.aimailabs.nexabase.file.service;

import io.github.aimailabs.nexabase.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileRecordService {
    FileRecord upload(MultipartFile file, Long tenantId, Long userId) throws Exception;
    InputStream download(Long fileId) throws Exception;
    void deleteFile(Long fileId, Long userId);
}
