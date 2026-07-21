package io.github.aimailabs.nexabase.file.api.dto;
import lombok.Data;
import java.io.Serializable;

@Data
public class FileRecordDTO implements Serializable {
    private Long id;
    private String fileName;
    private String url;
}
