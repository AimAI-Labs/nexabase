package com.aimai.nexabase.document.entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentEntityTest {
    @Test
    public void testDocumentEntityCreation() {
        DocInfo doc = new DocInfo();
        doc.setTitle("Design Doc");
        assertEquals("Design Doc", doc.getTitle());
    }
}
