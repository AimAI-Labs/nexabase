package io.github.aimailabs.nexabase.document.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import io.github.aimailabs.nexabase.document.api.event.DocumentChangedEvent;
import io.github.aimailabs.nexabase.document.entity.DocContent;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.mapper.DocInfoMapper;
import io.github.aimailabs.nexabase.document.repository.DocContentRepository;
import io.github.aimailabs.nexabase.document.service.impl.DocInfoServiceImpl;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        mockDoc.setCategoryId(1L);
        mockDoc.setTitle("Test Doc");
        mockDoc.setAuthorId(100L);
        mockDoc.setIsDeleted(0);
    }

    @Test
    @DisplayName("创建文档成功 - 需向 MQ 正确投递 CREATE 事件")
    void testCreate_Success_ShouldPublishCreateEvent() {
        // Arrange
        when(docInfoMapper.exists(any())).thenReturn(false);
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

        // 断言 MQ 消息投递细节
        ArgumentCaptor<DocumentChangedEvent> eventCaptor = ArgumentCaptor.forClass(DocumentChangedEvent.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(DocInfoServiceImpl.EXCHANGE_NAME),
            eq(DocInfoServiceImpl.ROUTING_KEY),
            eventCaptor.capture()
        );

        DocumentChangedEvent event = eventCaptor.getValue();
        assertEquals(10L, event.getDocumentId());
        assertEquals("CREATE", event.getAction());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("创建文档失败（标题重复） - 不应向 MQ 投递任何消息")
    void testCreate_TitleExists_ShouldNotPublishEvent() {
        // Arrange
        when(docInfoMapper.exists(any())).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> docInfoService.create(mockDoc, "Test Content"));

        verify(docInfoMapper, never()).insert(any());
        verify(docContentRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("更新文档成功 - 需向 MQ 正确投递 UPDATE 事件")
    void testUpdate_Success_ShouldPublishUpdateEvent() {
        // Arrange
        DocInfo updateDoc = new DocInfo();
        updateDoc.setTitle("Updated Title");

        when(docInfoMapper.selectById(10L)).thenReturn(mockDoc);
        when(docInfoMapper.exists(any())).thenReturn(false);
        when(docInfoMapper.updateById(any(DocInfo.class))).thenReturn(1);

        DocContent existingContent = new DocContent();
        existingContent.setDocumentId(10L);
        existingContent.setContent("Old Content");
        when(docContentRepository.findById(10L)).thenReturn(Optional.of(existingContent));
        when(docContentRepository.save(any(DocContent.class))).thenReturn(existingContent);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Act
        DocInfo result = docInfoService.update(10L, updateDoc, "New Content", 200L);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());

        verify(docInfoMapper, times(1)).updateById(mockDoc);
        verify(docContentRepository, times(1)).save(argThat(c -> "New Content".equals(c.getContent())));

        // 断言 MQ 消息投递细节
        ArgumentCaptor<DocumentChangedEvent> eventCaptor = ArgumentCaptor.forClass(DocumentChangedEvent.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(DocInfoServiceImpl.EXCHANGE_NAME),
            eq(DocInfoServiceImpl.ROUTING_KEY),
            eventCaptor.capture()
        );

        DocumentChangedEvent event = eventCaptor.getValue();
        assertEquals(10L, event.getDocumentId());
        assertEquals("UPDATE", event.getAction());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("更新文档失败（文档不存在） - 不应向 MQ 投递任何消息")
    void testUpdate_DocNotFound_ShouldNotPublishEvent() {
        // Arrange
        when(docInfoMapper.selectById(10L)).thenReturn(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> docInfoService.update(10L, mockDoc, "Content", 200L));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("删除文档成功 - 需向 MQ 正确投递 DELETE 事件")
    void testDelete_Success_ShouldPublishDeleteEvent() {
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

        // 断言 MQ 消息投递细节
        ArgumentCaptor<DocumentChangedEvent> eventCaptor = ArgumentCaptor.forClass(DocumentChangedEvent.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq(DocInfoServiceImpl.EXCHANGE_NAME),
            eq(DocInfoServiceImpl.ROUTING_KEY),
            eventCaptor.capture()
        );

        DocumentChangedEvent event = eventCaptor.getValue();
        assertEquals(10L, event.getDocumentId());
        assertEquals("DELETE", event.getAction());
        assertNotNull(event.getTimestamp());
    }

    @Test
    @DisplayName("删除文档失败（重复删除或文档不存在） - 不应向 MQ 投递任何消息")
    void testDelete_AlreadyDeleted_ShouldNotPublishEvent() {
        // Arrange
        mockDoc.setIsDeleted(1);
        when(docInfoMapper.selectById(10L)).thenReturn(mockDoc);

        // Act & Assert
        assertThrows(BusinessException.class, () -> docInfoService.delete(10L, 200L));

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}

