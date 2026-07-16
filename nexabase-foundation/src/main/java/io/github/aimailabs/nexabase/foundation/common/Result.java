package io.github.aimailabs.nexabase.foundation.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 统一 API 响应封装。
 * <p>
 * 所有接口（无论成功或失败）均返回此结构，确保前端/调用方可统一解析。
 * 响应中携带 {@code traceId}，将客户端可见的错误与服务器端日志链路关联。
 * <p>
 * JSON 结构示例：
 * <pre>{@code
 * {
 *   "code": 200,
 *   "message": "success",
 *   "data": { ... },
 *   "traceId": "a1b2c3d4e5f67890a1b2c3d4e5f67890"
 * }
 * }</pre>
 *
 * @param <T> 响应数据类型
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 业务状态码（非 HTTP 状态码），200 表示成功 */
    private int code;

    /** 提示消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 链路追踪 ID，用于关联请求链路的日志与异常 */
    private String traceId;

    // ==================== 成功响应 ====================

    /**
     * 构建成功响应（带数据）。
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, TraceContext.getTraceId());
    }

    /**
     * 构建成功响应（无数据）。
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null, TraceContext.getTraceId());
    }

    /**
     * 构建成功响应（自定义消息与数据）。
     *
     * @param message 提示消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data, TraceContext.getTraceId());
    }

    // ==================== 错误响应 ====================

    /**
     * 构建错误响应（指定错误码与自定义消息）。
     *
     * @param resultCode 错误码枚举
     * @param message    错误消息
     * @param <T>        数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null, TraceContext.getTraceId());
    }

    /**
     * 构建错误响应（使用错误码的默认消息）。
     *
     * @param resultCode 错误码枚举
     * @param <T>        数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, TraceContext.getTraceId());
    }

    /**
     * 构建错误响应（指定错误码数值与消息）。
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, TraceContext.getTraceId());
    }
}
