package io.github.aimailabs.nexabase.file.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileRecordTest {
    @Test
    public void testFileRecordCreation() {
        FileRecord record = new FileRecord();
        record.setFileName("demo.pdf");
        assertEquals("demo.pdf", record.getFileName());
    }
}
