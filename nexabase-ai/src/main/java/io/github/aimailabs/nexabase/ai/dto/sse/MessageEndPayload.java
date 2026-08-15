package io.github.aimailabs.nexabase.ai.dto.sse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * SSE 问答结束与统计元数据 Payload。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSE 问答结束与统计载荷")
public class MessageEndPayload {

    @Schema(description = "会话 UUID", example = "sess_8a7f6c3b2e")
    private String sessionId;

    @Schema(description = "用户提问消息 UUID", example = "msg_req_3d2c1b")
    private String userMessageId;

    @Schema(description = "AI 回答消息 UUID", example = "msg_res_9b4e1a")
    private String assistantMessageId;

    @Schema(description = "意图改写后的独立 Query", example = "Nacos 3.0 支持哪些集群部署模式？")
    private String rewriteQuery;

    @Schema(description = "Token 消耗统计 (inputTokens, outputTokens, totalTokens)")
    private Map<String, Integer> usage;

    @Schema(description = "各阶段耗时统计(ms) (retrieveCostMs, llmCostMs, totalCostMs)")
    private Map<String, Long> metrics;
}
