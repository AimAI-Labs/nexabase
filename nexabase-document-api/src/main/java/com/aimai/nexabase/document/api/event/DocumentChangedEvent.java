package com.aimai.nexabase.document.api.event;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChangedEvent implements Serializable {
    private Long documentId;
    private String action; // 枚举或字符串：CREATE, UPDATE, DELETE, PUBLISH
    private Long timestamp;
}
