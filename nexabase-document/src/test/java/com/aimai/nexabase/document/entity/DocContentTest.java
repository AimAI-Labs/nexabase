package com.aimai.nexabase.document.entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocContentTest {
    @Test
    public void testDocContentCreation() {
        DocContent content = new DocContent();
        content.setDocumentId(1L);
        assertEquals(1L, content.getDocumentId());
    }
}
