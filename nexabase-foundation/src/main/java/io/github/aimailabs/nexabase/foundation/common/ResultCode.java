package io.github.aimailabs.nexabase.foundation.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 统一错误码枚举。
 * <p>
 * 采用五位编码方案，前三位对齐 HTTP 状态码语义，后两位为业务子码：
 * <ul>
 *   <li>{@code 200}   — 成功</li>
 *   <li>{@code 40xxx} — 客户端错误（4xx）</li>
 *   <li>{@code 50xxx} — 服务端错误（5xx）</li>
 * </ul>
 * 每个错误码同时绑定 HTTP 状态码，确保业务码与 HTTP 语义一致。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /* ==================== 成功 ==================== */
    SUCCESS(200, "success", HttpStatus.OK),

    /* ==================== 客户端错误（4xx） ==================== */
    BAD_REQUEST(40000, "请求参数错误", HttpStatus.BAD_REQUEST),
    VALIDATION_FAILED(40001, "参数校验失败", HttpStatus.BAD_REQUEST),
    PARAM_MISSING(40002, "必填参数缺失", HttpStatus.BAD_REQUEST),
    PARAM_TYPE_MISMATCH(40003, "参数类型不匹配", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED(40100, "未认证或认证已过期", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(40101, "令牌无效", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(40102, "令牌已过期", HttpStatus.UNAUTHORIZED),

    FORBIDDEN(40300, "无访问权限", HttpStatus.FORBIDDEN),

    NOT_FOUND(40400, "请求资源不存在", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND(40401, "业务资源不存在", HttpStatus.NOT_FOUND),

    METHOD_NOT_ALLOWED(40500, "请求方法不被允许", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_SUPPORTED(41500, "不支持的媒体类型", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    CONFLICT(40900, "资源状态冲突", HttpStatus.CONFLICT),
    TOO_MANY_REQUESTS(42900, "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),

    /* ==================== 服务端错误（5xx） ==================== */
    INTERNAL_ERROR(50000, "系统内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_ERROR(50001, "业务处理异常", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(50002, "数据访问异常", HttpStatus.INTERNAL_SERVER_ERROR),
    REMOTE_CALL_ERROR(50003, "远程调用异常", HttpStatus.INTERNAL_SERVER_ERROR),

    SERVICE_UNAVAILABLE(50300, "服务暂不可用", HttpStatus.SERVICE_UNAVAILABLE),
    GATEWAY_TIMEOUT(50400, "网关请求超时", HttpStatus.GATEWAY_TIMEOUT),
    ;

    /** 业务状态码 */
    private final int code;

    /** 默认提示消息 */
    private final String message;

    /** 对应的 HTTP 状态码 */
    private final HttpStatus httpStatus;
}
