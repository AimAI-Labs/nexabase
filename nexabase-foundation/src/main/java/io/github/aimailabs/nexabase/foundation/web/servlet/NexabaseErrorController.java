package io.github.aimailabs.nexabase.foundation.web.servlet;

import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servlet 错误控制器。
 * <p>
 * 处理未被 {@link ServletGlobalExceptionHandler} 拦截的错误，
 * 主要覆盖以下场景：
 * <ul>
 *   <li>404 无匹配路由（未启用 {@code throw-exception-if-no-handler-found} 时）</li>
 *   <li>Filter 链中抛出的异常</li>
 *   <li>静态资源访问错误</li>
 * </ul>
 * <p>
 * 替代 Spring Boot 默认的 {@code BasicErrorController}，
 * 确保所有错误响应统一为 {@link Result} 格式并携带 TraceID。
 * <p>
 * 从请求属性恢复 error dispatch 时丢失的 TraceID（由 {@link TraceFilter} 存入）。
 */
@Slf4j
@RestController
public class NexabaseErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Result<Void>> handleError(HttpServletRequest request) {
        // 从请求属性恢复 TraceID（TraceFilter 在初始 dispatch 时存入，error dispatch 时可读取）
        String traceId = (String) request.getAttribute(TraceConstants.TRACE_ID_HEADER);
        if (traceId != null) {
            TraceContext.setTraceId(traceId);
        } else {
            traceId = TraceContext.getTraceId();
        }

        // 获取错误状态码
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusCode instanceof Integer ? (Integer) statusCode : 500;

        // 获取异常对象
        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        ResultCode resultCode = mapStatusToResultCode(status);
        String message = throwable != null ? throwable.getMessage() : resultCode.getMessage();

        if (throwable != null) {
            log.error("[{}] 错误控制器处理: {} {} → {}", traceId, request.getMethod(), requestUri, throwable.getMessage(), throwable);
        } else {
            log.warn("[{}] 错误控制器处理: {} {} → {}", traceId, request.getMethod(), requestUri, status);
        }

        Result<Void> result = Result.error(resultCode, message);
        result.setTraceId(traceId);
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(result);
    }

    private ResultCode mapStatusToResultCode(int status) {
        return switch (status) {
            case 400 -> ResultCode.BAD_REQUEST;
            case 401 -> ResultCode.UNAUTHORIZED;
            case 403 -> ResultCode.FORBIDDEN;
            case 404 -> ResultCode.NOT_FOUND;
            case 405 -> ResultCode.METHOD_NOT_ALLOWED;
            case 415 -> ResultCode.MEDIA_TYPE_NOT_SUPPORTED;
            case 429 -> ResultCode.TOO_MANY_REQUESTS;
            case 503 -> ResultCode.SERVICE_UNAVAILABLE;
            case 504 -> ResultCode.GATEWAY_TIMEOUT;
            default -> ResultCode.INTERNAL_ERROR;
        };
    }
}
