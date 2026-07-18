package io.github.aimailabs.nexabase.foundation.web.reactive;

import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.web.BaseExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.util.stream.Collectors;

/**
 * Reactive（Spring WebFlux）全局异常处理器。
 * <p>
 * 通过 {@code @RestControllerAdvice} 拦截 Controller 层抛出的异常，
 * 统一转换为 {@link Result} 响应格式并携带 TraceID。
 * <p>
 * 继承 {@link BaseExceptionHandler} 获得通用异常处理能力，
 * 本类追加 WebFlux 栈特有的异常类型处理（响应式参数绑定、状态码异常等）。
 * <p>
 * 对于 Controller 层之外的异常（如网关路由 404、下游服务不可达），
 * 由 {@link NexabaseErrorWebExceptionHandler} 统一处理。
 */
@Slf4j
@RestControllerAdvice
public class ReactiveGlobalExceptionHandler extends BaseExceptionHandler {

    /**
     * 处理 WebFlux 参数绑定校验失败。
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<Result<Void>> handleWebExchangeBind(WebExchangeBindException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", detail);
        Result<Void> result = Result.error(ResultCode.VALIDATION_FAILED, detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理必填参数缺失。
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<Result<Void>> handleServerWebInput(ServerWebInputException e) {
        log.warn("请求参数缺失: {}", e.getReason());
        Result<Void> result = Result.error(ResultCode.PARAM_MISSING, "必填参数缺失");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理 Spring WebFlux 响应状态码异常。
     * <p>
     * 常见于网关路由未匹配（404）、下游服务不可达（503）等场景。
     * <p>
     * 对于静态资源不存在的 404（如 Chrome DevTools 的 {@code .well-known} 请求），
     * 降级为 DEBUG 级别，避免正常的浏览器探测请求产生日志噪音。
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> handleResponseStatus(ResponseStatusException e) {
        int statusCode = e.getStatusCode().value();
        String reason = e.getReason();
        if (statusCode == 404 && reason != null && reason.contains("No static resource")) {
            log.debug("静态资源不存在 [404]: {}", reason);
        } else {
            log.warn("响应状态异常 [{}]: {}", statusCode, reason);
        }
        ResultCode resultCode = mapStatusCode(statusCode);
        String message = reason != null ? reason : resultCode.getMessage();
        Result<Void> result = Result.error(resultCode, message);
        return ResponseEntity.status(e.getStatusCode()).body(result);
    }

    private ResultCode mapStatusCode(int statusCode) {
        return switch (statusCode) {
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
