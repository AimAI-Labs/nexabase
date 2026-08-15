package io.github.aimailabs.nexabase.ai.dto.sse;

import io.github.aimailabs.nexabase.ai.dto.RagSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SSE 知识库引用切片推送 Payload。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSE 知识库引用切片载荷")
public class CitationEventPayload {

    @Schema(description = "会话 UUID", example = "sess_8a7f6c3b2e")
    private String sessionId;

    @Schema(description = "引用切片列表")
    private List<RagSource> sources;
}
