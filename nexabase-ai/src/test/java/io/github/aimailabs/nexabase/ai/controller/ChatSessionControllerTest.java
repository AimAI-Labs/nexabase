package io.github.aimailabs.nexabase.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.ai.dto.ChatSessionDTO;
import io.github.aimailabs.nexabase.ai.dto.CreateSessionRequest;
import io.github.aimailabs.nexabase.ai.dto.UpdateSessionRequest;
import io.github.aimailabs.nexabase.ai.service.ChatSessionService;
import io.github.aimailabs.nexabase.foundation.security.ServletUserContextFilter;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatSessionController.class)
@Import(ServletUserContextFilter.class)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/ai/chat/session 创建会话成功")
    void testCreateSession() throws Exception {
        CreateSessionRequest req = new CreateSessionRequest();
        req.setTitle("微服务会话");

        ChatSessionDTO respDTO = new ChatSessionDTO();
        respDTO.setSessionId("sess_abc123");
        respDTO.setTitle("微服务会话");
        respDTO.setUserId(10001L);

        when(sessionService.createSession(any(), eq(10001L))).thenReturn(respDTO);

        mockMvc.perform(post("/api/v1/ai/chat/session")
                        .header(UserContext.USER_ID_HEADER, "10001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sessionId").value("sess_abc123"))
                .andExpect(jsonPath("$.data.title").value("微服务会话"));
    }

    @Test
    @DisplayName("GET /api/v1/ai/chat/sessions 查询会话列表成功")
    void testListSessions() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setSessionId("sess_001");
        dto.setTitle("会话1");

        Page<ChatSessionDTO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(dto));

        when(sessionService.listSessions(eq(10001L), any(), eq(1), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/ai/chat/sessions")
                        .header(UserContext.USER_ID_HEADER, "10001")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].sessionId").value("sess_001"));
    }

    @Test
    @DisplayName("PUT /api/v1/ai/chat/session/{sessionId} 更新会话配置")
    void testUpdateSession() throws Exception {
        UpdateSessionRequest req = new UpdateSessionRequest();
        req.setTitle("新标题");

        when(sessionService.updateSession(eq("sess_001"), any(), eq(10001L))).thenReturn(true);

        mockMvc.perform(put("/api/v1/ai/chat/session/sess_001")
                        .header(UserContext.USER_ID_HEADER, "10001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/ai/chat/session/{sessionId} 逻辑删除会话")
    void testDeleteSession() throws Exception {
        when(sessionService.deleteSession(eq("sess_001"), eq(10001L))).thenReturn(true);

        mockMvc.perform(delete("/api/v1/ai/chat/session/sess_001")
                        .header(UserContext.USER_ID_HEADER, "10001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}
