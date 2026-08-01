package io.github.aimailabs.nexabase.file.service;

import io.github.aimailabs.nexabase.file.entity.FileRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务接口。
 */
public interface FileRecordService {

    /**
     * 上传文件。
     *
     * @param file     上传的文件
     * @param tenantId 租户ID
     * @param userId   上传人ID
     * @return 文件记录
     */
    FileRecord upload(MultipartFile file, Long tenantId, Long userId) throws Exception;

    /**
     * 下载文件。
     *
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream download(Long fileId) throws Exception;

    /**
     * 根据ID查询文件记录。
     *
     * @param fileId 文件ID
     * @return 文件记录
     */
    FileRecord getById(Long fileId);

    /**
     * 逻辑删除文件。
     *
     * @param fileId 文件ID
     * @param userId 操作人ID
     */
    void deleteFile(Long fileId, Long userId);

    /**
     * 从对象存储下载文件字节并解析为纯文本。
     * <p>根据文件 MIME 类型和扩展名自动选择解析器。
     * 不支持的格式返回空字符串。
     *
     * @param fileId 文件ID
     * @return 解析后的纯文本内容
     */
    String parseFileContent(Long fileId);
}
