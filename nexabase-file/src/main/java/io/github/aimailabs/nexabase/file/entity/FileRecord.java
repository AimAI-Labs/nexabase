package io.github.aimailabs.nexabase.file.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file_record")
public class FileRecord {
    private Long id;
    private String fileName;
    private String md5;
    private Long size;
    private String contentType;
    private String bucket;
    private String objectPath;
    private Long tenantId;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
}
