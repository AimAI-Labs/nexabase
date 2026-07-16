package io.github.aimailabs.nexabase.foundation.exception;

import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;

/**
 * 业务异常。
 * <p>
 * 用于表示可预见的业务规则违反，如资源不存在、参数不合法、状态冲突等。
 * 对应 HTTP 4xx 状态码，异常被全局处理器捕获后返回标准错误响应。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 使用预定义错误码
 * throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "人物不存在: " + name);
 *
 * // 简写（默认 BAD_REQUEST）
 * throw new BusinessException("参数不合法: " + field);
 * }</pre>
 */
public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    /**
     * 使用自定义消息，默认错误码为 {@link ResultCode#BAD_REQUEST}。
     *
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super(message);
        this.resultCode = ResultCode.BAD_REQUEST;
    }

    /**
     * 使用指定错误码与默认消息。
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 使用指定错误码与自定义消息。
     *
     * @param resultCode 错误码枚举
     * @param message    异常消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 使用指定错误码、自定义消息与根因异常。
     *
     * @param resultCode 错误码枚举
     * @param message    异常消息
     * @param cause      根因异常
     */
    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }

    /**
     * 获取错误码枚举。
     *
     * @return 错误码
     */
    public ResultCode getResultCode() {
        return resultCode;
    }
}
