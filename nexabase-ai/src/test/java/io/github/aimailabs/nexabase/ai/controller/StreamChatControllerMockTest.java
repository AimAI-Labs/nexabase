package io.github.aimailabs.nexabase.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.ai.dto.StreamChatRequest;
import io.github.aimailabs.nexabase.ai.service.StreamChatService;
import io.github.aimailabs.nexabase.foundation.security.ServletUserContextFilter;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StreamChatController.class)
@Import(ServletUserContextFilter.class)
class StreamChatControllerMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StreamChatService streamChatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/ai/chat/stream 建立 SSE 连接并返回 200")
    void testChatStream_EstablishSseConnection() throws Exception {
        StreamChatRequest req = new StreamChatRequest();
        req.setSessionId("sess_test_stream");
        req.setQuery("什么是 Spring Boot？");

        SseEmitter emitter = new SseEmitter(10000L);
        when(streamChatService.chatStream(any(), eq(10001L))).thenReturn(emitter);

        mockMvc.perform(post("/api/v1/ai/chat/stream")
                        .header(UserContext.USER_ID_HEADER, "10001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
