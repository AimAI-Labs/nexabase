package io.github.aimailabs.nexabase.document.api.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DocumentChangedEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSerializationAndDeserialization() throws Exception {
        // Arrange
        DocumentChangedEvent originalEvent = new DocumentChangedEvent(12345L, "CREATE", System.currentTimeMillis());

        // Act
        String json = objectMapper.writeValueAsString(originalEvent);
        DocumentChangedEvent deserializedEvent = objectMapper.readValue(json, DocumentChangedEvent.class);

        // Assert
        assertNotNull(deserializedEvent);
        assertEquals(originalEvent.getDocumentId(), deserializedEvent.getDocumentId());
        assertEquals(originalEvent.getAction(), deserializedEvent.getAction());
        assertEquals(originalEvent.getTimestamp(), deserializedEvent.getTimestamp());
    }
}
