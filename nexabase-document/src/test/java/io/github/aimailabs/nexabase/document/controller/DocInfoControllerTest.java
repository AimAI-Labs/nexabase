package io.github.aimailabs.nexabase.document.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.document.entity.DocInfo;
import io.github.aimailabs.nexabase.document.service.DocInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocInfoController.class)
public class DocInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocInfoService docInfoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateDoc() throws Exception {
        DocInfo mockDoc = new DocInfo();
        mockDoc.setId(10L);
        mockDoc.setTitle("Test Doc");

        DocInfoController.CreateDocRequest request = new DocInfoController.CreateDocRequest();
        request.setDocInfo(mockDoc);
        request.setContent("Test Content");

        when(docInfoService.create(any(DocInfo.class), anyString())).thenReturn(mockDoc);

        mockMvc.perform(post("/api/v1/document/doc")
                .header("X-User-Id", "99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("Test Doc"));

        verify(docInfoService, times(1)).create(any(DocInfo.class), eq("Test Content"));
    }

    @Test
    void testGetDoc() throws Exception {
        DocInfo mockDoc = new DocInfo();
        mockDoc.setId(10L);
        mockDoc.setTitle("Test Doc");

        when(docInfoService.getById(10L)).thenReturn(mockDoc);

        mockMvc.perform(get("/api/v1/document/doc/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10));

        verify(docInfoService, times(1)).getById(10L);
    }

    @Test
    void testDeleteDoc() throws Exception {
        doNothing().when(docInfoService).delete(10L, 99L);

        mockMvc.perform(delete("/api/v1/document/doc/10")
                .header("X-User-Id", "99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(docInfoService, times(1)).delete(10L, 99L);
    }
}
