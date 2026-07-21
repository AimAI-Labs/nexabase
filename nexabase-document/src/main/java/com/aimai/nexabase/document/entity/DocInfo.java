package com.aimai.nexabase.document.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_info")
public class DocInfo {
    private Long id;
    private Long kbId;
    private Long categoryId;
    private Long tenantId;
    private String title;
    private Long authorId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
}
