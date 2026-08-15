package io.github.aimailabs.nexabase.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消息反馈请求。
 */
@Data
@Schema(description = "消息反馈请求")
public class FeedbackRequest {

    @Schema(description = "反馈状态 (0: 取消/无反馈, 1: 点赞, 2: 点踩)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer feedbackStatus;

    @Schema(description = "反馈原因/备注 (可选)", example = "回答非常准确，引用来源清晰")
    private String feedbackRemark;
}
