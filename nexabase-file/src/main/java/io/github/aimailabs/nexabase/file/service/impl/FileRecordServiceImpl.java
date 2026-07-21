package io.github.aimailabs.nexabase.file.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.file.entity.FileRecord;
import io.github.aimailabs.nexabase.file.mapper.FileRecordMapper;
import io.github.aimailabs.nexabase.file.service.FileRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileRecordServiceImpl implements FileRecordService {

    private final FileRecordMapper fileRecordMapper;
    private final S3Client s3Client;
    private static final String BUCKET_NAME = "nexabase";

    @Override
    public FileRecord upload(MultipartFile file, Long tenantId, Long userId) throws Exception {
        String md5 = DigestUtil.md5Hex(file.getInputStream());
        
        // 秒传逻辑：检查 MD5 是否存在
        FileRecord existRecord = fileRecordMapper.selectOne(
            new LambdaQueryWrapper<FileRecord>().eq(FileRecord::getMd5, md5)
        );
        if (existRecord != null) {
            return existRecord; // 实际场景可能需要新建记录并复用 ObjectPath
        }

        String objectPath = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        
        // 上传到 RustFS (S3)
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(objectPath)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

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

    @Override
    public InputStream download(Long fileId) throws Exception {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record == null) {
            throw new RuntimeException("File not found");
        }
        return s3Client.getObject(
            GetObjectRequest.builder().bucket(record.getBucket()).key(record.getObjectPath()).build()
        );
    }

    @Override
    public void deleteFile(Long fileId, Long userId) {
        FileRecord record = fileRecordMapper.selectById(fileId);
        if (record != null) {
            // 逻辑删除
            record.setIsDeleted(1);
            record.setUpdatedBy(userId);
            fileRecordMapper.updateById(record);
        }
    }
}
