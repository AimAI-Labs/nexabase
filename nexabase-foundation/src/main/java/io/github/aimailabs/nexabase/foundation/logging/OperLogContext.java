package io.github.aimailabs.nexabase.foundation.logging;

import lombok.Builder;
import lombok.Data;

/**
 * 操作日志上下文载体。
 * <p>
 * 由 {@link OperLogAspect} 采集后传递给 {@link OperLogRecorder} 落库，
 * 解耦 AOP 切面与具体存储实现。
 */
@Data
@Builder
public class OperLogContext {

    /** 操作人 ID */
    private Long userId;

    /** 操作人名称 */
    private String username;

    /** 业务模块 */
    private String module;

    /** 操作动作 */
    private String action;

    /** HTTP 请求方法 */
    private String method;

    /** 请求 URL */
    private String url;

    /** 操作 IP 地址 */
    private String ipAddress;

    /** 请求参数（JSON，已脱敏） */
    private String requestParams;

    /** 响应结果（JSON，已截断） */
    private String responseResult;

    /** 操作状态：1-成功, 0-失败 */
    private Integer status;

    /** 错误信息（失败时记录） */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costTime;
}
