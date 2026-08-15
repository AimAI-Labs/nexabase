package io.github.aimailabs.nexabase.ai.dto.sse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 思考/意图改写事件 Payload。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSE 思考/改写事件载荷")
public class ThoughtEventPayload {

    @Schema(description = "处理阶段 (如 rewrite, retrieve, prompt)", example = "rewrite")
    private String stage;

    @Schema(description = "思考或意图改写内容", example = "正在将问题重写为独立查询：'Nacos 3.0 支持哪些集群部署模式？'")
    private String thought;
}
