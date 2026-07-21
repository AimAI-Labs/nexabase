package io.github.aimailabs.nexabase.document.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("doc_category")
public class DocCategory {
    private Long id;
    private Long kbId;
    private Long tenantId;
    private Long parentId;
    private String path;
    private String name;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Integer isDeleted;
}
