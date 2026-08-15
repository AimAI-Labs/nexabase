package io.github.aimailabs.nexabase.ai.dto.sse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * SSE 增量 Token 推送 Payload (兼容 OpenAI chunk 格式)。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSE 增量 Token 载荷")
public class MessageDeltaPayload {

    @Schema(description = "消息 UUID", example = "msg_res_9b4e1a")
    private String id;

    @Schema(description = "角色", example = "assistant")
    private String role;

    @Schema(description = "增量内容片段", example = "{\"content\":\"根据\"}")
    private Map<String, String> delta;

    @Schema(description = "Token 顺序索引", example = "0")
    private Integer index;

    public static MessageDeltaPayload of(String messageId, String role, String content, int index) {
        return new MessageDeltaPayload(messageId, role, Map.of("content", content), index);
    }
}
