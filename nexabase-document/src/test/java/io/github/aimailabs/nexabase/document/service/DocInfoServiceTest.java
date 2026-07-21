package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.api.event.DocumentChangedEvent;
import io.github.aimailabs.nexabase.document.entity.DocContent;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.mapper.DocInfoMapper;
import io.github.aimailabs.nexabase.document.repository.DocContentRepository;
import io.github.aimailabs.nexabase.document.service.impl.DocInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocInfoServiceTest {

    @Mock
    private DocInfoMapper docInfoMapper;

    @Mock
    private DocContentRepository docContentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DocInfoServiceImpl docInfoService;

    private DocInfo mockDoc;

    @BeforeEach
    void setUp() {
        mockDoc = new DocInfo();
        mockDoc.setId(10L);
        mockDoc.setTitle("Test Doc");
        mockDoc.setAuthorId(100L);
    }

    @Test
    void testCreate() {
        // Arrange
        when(docInfoMapper.insert(any(DocInfo.class))).thenReturn(1);
        when(docContentRepository.save(any(DocContent.class))).thenReturn(new DocContent());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Act
        DocInfo result = docInfoService.create(mockDoc, "Test Content");

        // Assert
        assertNotNull(result);
        assertEquals("Test Doc", result.getTitle());

        verify(docInfoMapper, times(1)).insert(mockDoc);
        verify(docContentRepository, times(1)).save(argThat(content -> 
            content.getDocumentId().equals(10L) && "Test Content".equals(content.getContent())
        ));
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(DocInfoServiceImpl.EXCHANGE_NAME),
            eq(DocInfoServiceImpl.ROUTING_KEY),
            argThat((DocumentChangedEvent event) -> event.getDocumentId().equals(10L) && "CREATE".equals(event.getAction()))
        );
    }

    @Test
    void testDelete() {
        // Arrange
        when(docInfoMapper.selectById(10L)).thenReturn(mockDoc);
        when(docInfoMapper.updateById(any(DocInfo.class))).thenReturn(1);
        doNothing().when(docContentRepository).deleteById(10L);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Act
        docInfoService.delete(10L, 200L);

        // Assert
        assertEquals(1, mockDoc.getIsDeleted());
        assertEquals(200L, mockDoc.getUpdatedBy());

        verify(docInfoMapper, times(1)).updateById(mockDoc);
        verify(docContentRepository, times(1)).deleteById(10L);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(DocInfoServiceImpl.EXCHANGE_NAME),
            eq(DocInfoServiceImpl.ROUTING_KEY),
            argThat((DocumentChangedEvent event) -> event.getDocumentId().equals(10L) && "DELETE".equals(event.getAction()))
        );
    }
}
