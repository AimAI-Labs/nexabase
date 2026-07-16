package io.github.aimailabs.nexabase.foundation.exception;

import io.github.aimailabs.nexabase.foundation.common.ResultCode;

/**
 * 系统异常。
 * <p>
 * 用于表示不可预见的系统级错误，如数据库连接失败、远程调用超时、空指针等。
 * 对应 HTTP 5xx 状态码，异常被全局处理器捕获后返回标准错误响应，
 * 并以 ERROR 级别记录完整堆栈，便于快速定位与排查。
 * <p>
 * 使用示例：
 * <pre>{@code
 * throw new SystemException(ResultCode.DATABASE_ERROR, "Neo4j 连接失败", e);
 * }</pre>
 */
public class SystemException extends RuntimeException {

    private final ResultCode resultCode;

    /**
     * 使用自定义消息，默认错误码为 {@link ResultCode#INTERNAL_ERROR}。
     *
     * @param message 异常消息
     */
    public SystemException(String message) {
        super(message);
        this.resultCode = ResultCode.INTERNAL_ERROR;
    }

    /**
     * 使用指定错误码与默认消息。
     *
     * @param resultCode 错误码枚举
     */
    public SystemException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 使用指定错误码与自定义消息。
     *
     * @param resultCode 错误码枚举
     * @param message    异常消息
     */
    public SystemException(ResultCode resultCode, String message) {
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
    public SystemException(ResultCode resultCode, String message, Throwable cause) {
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
