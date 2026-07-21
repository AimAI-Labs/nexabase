package com.aimai.nexabase.document.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_knowledge_base")
public class DocKnowledgeBase {
    private Long id;
    private String name;
    private String description;
    private Long tenantId;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
}
