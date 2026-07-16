package io.github.aimailabs.nexabase.foundation.web;

import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import io.github.aimailabs.nexabase.foundation.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 异常处理器基类，包含 Servlet 与 Reactive 栈共用的通用异常处理逻辑。
 * <p>
 * 子类（{@link io.github.aimailabs.nexabase.foundation.web.servlet.ServletGlobalExceptionHandler}
 * 与 {@link io.github.aimailabs.nexabase.foundation.web.reactive.ReactiveGlobalExceptionHandler}）
 * 通过继承自动获得这些 {@code @ExceptionHandler} 方法，并追加各自栈特有的异常处理。
 * <p>
 * 所有处理方法均通过 {@link Result} 封装错误响应，并携带 MDC 中的 TraceID，
 * 实现异常堆栈与请求链路日志的上下文关联。
 */
@Slf4j
public abstract class BaseExceptionHandler {

    /**
     * 处理业务异常（可预见的业务规则违反）。
     * <p>
     * 以 WARN 级别记录（无需堆栈），HTTP 状态码取自 {@link BusinessException#getResultCode()}。
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常 [{}]: {}", e.getResultCode().getCode(), e.getMessage());
        Result<Void> result = Result.error(e.getResultCode(), e.getMessage());
        return ResponseEntity.status(e.getResultCode().getHttpStatus()).body(result);
    }

    /**
     * 处理系统异常（不可预见的系统级错误）。
     * <p>
     * 以 ERROR 级别记录完整堆栈，便于快速定位与排查。
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Void>> handleSystemException(SystemException e) {
        log.error("系统异常 [{}]: {}", e.getResultCode().getCode(), e.getMessage(), e);
        Result<Void> result = Result.error(e.getResultCode(), e.getMessage());
        return ResponseEntity.status(e.getResultCode().getHttpStatus()).body(result);
    }

    /**
     * 处理非法参数异常。
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        Result<Void> result = Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理空指针异常。
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Void>> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: {}", e.getMessage(), e);
        Result<Void> result = Result.error(ResultCode.INTERNAL_ERROR, "系统内部错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }

    /**
     * 兜底异常处理：捕获所有未被前面处理器匹配的异常。
     * <p>
     * 以 ERROR 级别记录完整堆栈，返回 500 错误响应。
     * 对外不暴露内部堆栈信息，仅返回通用提示。
     */
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未处理异常: {}", e.getMessage(), e);
        Result<Void> result = Result.error(ResultCode.INTERNAL_ERROR, "系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
    }
}
