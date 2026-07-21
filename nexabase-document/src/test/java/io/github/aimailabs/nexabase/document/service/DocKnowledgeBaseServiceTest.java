package io.github.aimailabs.nexabase.document.service;

import io.github.aimailabs.nexabase.document.entity.DocKnowledgeBase;
import io.github.aimailabs.nexabase.document.mapper.DocKnowledgeBaseMapper;
import io.github.aimailabs.nexabase.document.service.impl.DocKnowledgeBaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocKnowledgeBaseServiceTest {

    @Mock
    private DocKnowledgeBaseMapper mapper;

    @InjectMocks
    private DocKnowledgeBaseServiceImpl service;

    private DocKnowledgeBase mockKb;

    @BeforeEach
    void setUp() {
        mockKb = new DocKnowledgeBase();
        mockKb.setId(1L);
        mockKb.setName("Test KB");
        mockKb.setOwnerId(100L);
        mockKb.setIsDeleted(0);
    }

    @Test
    void testCreate() {
        when(mapper.insert(any(DocKnowledgeBase.class))).thenReturn(1);

        DocKnowledgeBase result = service.create(mockKb);

        assertNotNull(result);
        assertEquals("Test KB", result.getName());
        verify(mapper, times(1)).insert(mockKb);
    }

    @Test
    void testGetById() {
        when(mapper.selectById(1L)).thenReturn(mockKb);

        DocKnowledgeBase result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(mapper, times(1)).selectById(1L);
    }

    @Test
    void testDelete() {
        when(mapper.selectById(1L)).thenReturn(mockKb);
        when(mapper.updateById(any(DocKnowledgeBase.class))).thenReturn(1);

        service.delete(1L, 200L);

        assertEquals(1, mockKb.getIsDeleted());
        assertEquals(200L, mockKb.getUpdatedBy());
        verify(mapper, times(1)).selectById(1L);
        verify(mapper, times(1)).updateById(mockKb);
    }
}
