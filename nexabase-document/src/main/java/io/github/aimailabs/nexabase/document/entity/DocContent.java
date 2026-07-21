package io.github.aimailabs.nexabase.document.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "doc_content")
public class DocContent {
    @Id
    private Long documentId; // 对应 MySQL 中 doc_info.id
    private String content;  // 长篇非结构化正文
}
