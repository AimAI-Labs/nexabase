package io.github.aimailabs.nexabase.foundation.web.servlet;

import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.web.BaseExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * Servlet（Spring MVC）全局异常处理器。
 * <p>
 * 通过 {@code @RestControllerAdvice} 拦截所有 Controller 层抛出的异常，
 * 统一转换为 {@link Result} 响应格式，并携带 TraceID。
 * <p>
 * 继承 {@link BaseExceptionHandler} 获得通用异常处理能力，
 * 本类追加 MVC 栈特有的异常类型处理（参数校验、类型转换、请求方法不支持等）。
 * <p>
 * 本类由 {@link io.github.aimailabs.nexabase.foundation.config.ServletWebAutoConfiguration}
 * 自动注册，引入 {@code nexabase-foundation} 依赖的 MVC 模块自动生效。
 */
@Slf4j
@RestControllerAdvice
public class ServletGlobalExceptionHandler extends BaseExceptionHandler {

    /**
     * 处理 {@code @RequestBody @Valid} 校验失败。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", detail);
        Result<Void> result = Result.error(ResultCode.VALIDATION_FAILED, detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理表单绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", detail);
        Result<Void> result = Result.error(ResultCode.VALIDATION_FAILED, detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理 {@code @Validated} 约束校验失败（路径参数 / 查询参数）。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String detail = e.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining("; "));
        log.warn("约束校验失败: {}", detail);
        Result<Void> result = Result.error(ResultCode.VALIDATION_FAILED, detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理必填查询参数缺失。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        String detail = "必填参数缺失: " + e.getParameterName();
        log.warn(detail);
        Result<Void> result = Result.error(ResultCode.PARAM_MISSING, detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理参数类型转换失败（如 path variable 无法转为 Long）。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String detail = "参数类型不匹配: " + e.getName();
        log.warn("{} (期望: {}, 实际: {})", detail,
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知",
                e.getValue());
        Result<Void> result = Result.error(ResultCode.PARAM_TYPE_MISMATCH, detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理请求体反序列化失败（JSON 格式错误）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        Result<Void> result = Result.error(ResultCode.BAD_REQUEST, "请求体格式错误或缺失");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    /**
     * 处理请求方法不被支持（如 POST 接口收到 GET 请求）。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        Result<Void> result = Result.error(ResultCode.METHOD_NOT_ALLOWED, e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(result);
    }

    /**
     * 处理不支持的媒体类型。
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的媒体类型: {}", e.getMessage());
        Result<Void> result = Result.error(ResultCode.MEDIA_TYPE_NOT_SUPPORTED, e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(result);
    }

    /**
     * 处理 404（需启用 {@code spring.mvc.throw-exception-if-no-handler-found=true}）。
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("请求资源不存在: {} {}", e.getHttpMethod(), e.getRequestURL());
        Result<Void> result = Result.error(ResultCode.NOT_FOUND, "请求资源不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 处理静态资源未找到（如 Spring Boot 3 默认不再提供 favicon.ico）。
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.warn("静态资源不存在: {} {}", e.getHttpMethod(), e.getResourcePath());
        Result<Void> result = Result.error(ResultCode.NOT_FOUND, "静态资源不存在");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

    /**
     * 处理文件上传超出大小限制。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        log.warn("文件上传超限: {}", e.getMessage());
        Result<Void> result = Result.error(ResultCode.BAD_REQUEST, "上传文件大小超出限制");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + ": " + violation.getMessage();
    }
}
