package io.github.aimailabs.nexabase.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.file.entity.FileRecord;
import io.github.aimailabs.nexabase.file.mapper.FileRecordMapper;
import io.github.aimailabs.nexabase.file.service.FileRecordService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.UUID;

/**
 * 文件服务实现类。
 * <p>
 * 核心功能：
 * <ul>
 *   <li>文件上传（含 MD5 秒传、租户隔离）</li>
 *   <li>文件下载（流式传输）</li>
 *   <li>文件逻辑删除</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileRecordServiceImpl implements FileRecordService {

    private final FileRecordMapper fileRecordMapper;
    private final S3Client s3Client;
    private static final String BUCKET_NAME = "nexabase";

    /**
     * 上传文件至对象存储（支持秒传）。
     * <p>
     * 秒传逻辑：同一租户下相同 MD5 的文件会复用物理存储，
     * 但仍为当前用户创建独立的文件记录，保证租户数据隔离。
     *
     * @param file     上传的文件
     * @param tenantId 租户ID
     * @param userId   上传人ID
     * @return 文件记录实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileRecord upload(MultipartFile file, Long tenantId, Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件为空");
        }

        String md5 = DigestUtil.md5Hex(file.getInputStream());

        // 秒传检测：同租户下相同 MD5 可复用 objectPath
        FileRecord existRecord = fileRecordMapper.selectOne(
                new LambdaQueryWrapper<FileRecord>()
                        .eq(FileRecord::getMd5, md5)
                        .eq(FileRecord::getTenantId, tenantId)
                        .eq(FileRecord::getIsDeleted, 0)
                        .last("LIMIT 1")
        );

        String objectPath;
        if (existRecord != null) {
            // 秒传：复用物理存储路径，但创建新记录
            log.info("文件秒传命中, MD5={}, 复用 objectPath={}", md5, existRecord.getObjectPath());
            objectPath = existRecord.getObjectPath();
        } else {
            // 新文件：生成唯一路径并上传至 S3
            objectPath = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(objectPath)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("文件上传成功, objectPath={}, size={}", objectPath, file.getSize());
        }

        // 为当前上传创建独立的 DB 记录
        FileRecord record = new FileRecord();
        record.setFileName(file.getOriginalFilename());
        record.setMd5(md5);
        record.setSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setBucket(BUCKET_NAME);
        record.setObjectPath(objectPath);
        record.setTenantId(tenantId);
        record.setCreatedBy(userId);

        fileRecordMapper.insert(record);
        return record;
    }

    /**
     * 下载文件（返回流）。
     *
     * @param fileId 文件记录ID
     * @return 文件输入流
     */
    @Override
    public InputStream download(Long fileId) throws Exception {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null || record.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "文件不存在或已被删除");
        }
        return s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(record.getBucket())
                        .key(record.getObjectPath())
                        .build()
        );
    }

    /**
     * 根据ID查询文件记录。
     *
     * @param fileId 文件ID
     * @return 文件记录
     */
    @Override
    public FileRecord getById(Long fileId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null || record.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "文件不存在或已被删除");
        }
        return record;
    }

    /**
     * 逻辑删除文件。
     * <p>
     * 注意：仅标记 DB 记录为已删除，不清理 S3 物理文件。
     * 物理清理可通过定时任务或事件驱动方式异步处理。
     *
     * @param fileId 文件ID
     * @param userId 操作人ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(Long fileId, Long userId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "文件不存在");
        }
        if (record.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.CONFLICT, "文件已被删除，无需重复操作");
        }

        record.setIsDeleted(1);
        record.setUpdatedBy(userId);
        fileRecordMapper.updateById(record);
        log.info("文件逻辑删除成功, fileId={}, objectPath={}", fileId, record.getObjectPath());
    }
}
