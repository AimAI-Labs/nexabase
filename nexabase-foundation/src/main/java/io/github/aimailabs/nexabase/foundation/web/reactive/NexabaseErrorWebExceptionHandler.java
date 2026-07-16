package io.github.aimailabs.nexabase.foundation.web.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * Reactive 全局错误处理器。
 * <p>
 * 实现 {@link WebExceptionHandler}，拦截 Controller 层之外抛出的所有异常，
 * 如网关路由未匹配（404）、下游代理失败（502/503）、Filter 链异常等。
 * <p>
 * 以最高优先级运行，在 Spring Boot 默认的 {@code DefaultErrorWebExceptionHandler} 之前处理，
 * 确保所有错误响应统一为 {@link Result} 格式并携带 TraceID。
 * <p>
 * TraceID 优先从 Reactor Context 获取（由 {@link ReactiveTraceFilter} 写入），
 * 回退到请求头，确保与请求链路日志关联。
 */
@Slf4j
@RequiredArgsConstructor
public class NexabaseErrorWebExceptionHandler implements WebExceptionHandler, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 响应已提交（如流式写入中途异常），无法再写入错误响应
        if (exchange.getResponse().isCommitted()) {
            log.error("响应已提交，无法处理异常: {}", ex.getMessage(), ex);
            return Mono.error(ex);
        }

        return Mono.deferContextual(contextView -> {
            // 从 Reactor Context 获取 TraceID
            String traceId = contextView.getOrDefault(TraceConstants.TRACE_ID_CONTEXT_KEY, "");
            if (traceId.isBlank()) {
                // 回退到请求头
                traceId = exchange.getRequest().getHeaders().getFirst(TraceConstants.TRACE_ID_HEADER);
            }
            if (traceId == null || traceId.isBlank()) {
                traceId = TraceContext.generateTraceId();
            }

            // 映射异常到错误码
            ResultCode resultCode = mapException(ex);
            String message = resolveMessage(ex, resultCode);

            // 记录异常日志（TraceID 通过 context-propagation 自动出现在日志中）
            if (resultCode.getHttpStatus().is5xxServerError()) {
                log.error("[{}] WebFlux 错误处理: {}", traceId, message, ex);
            } else {
                log.warn("[{}] WebFlux 错误处理: {}", traceId, message);
            }

            // 构建统一错误响应
            Result<Void> result = Result.error(resultCode, message);
            result.setTraceId(traceId);

            // 设置响应
            exchange.getResponse().setStatusCode(resultCode.getHttpStatus());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().add(TraceConstants.TRACE_ID_HEADER, traceId);

            // 序列化并写入
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(result);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(buffer));
            } catch (JsonProcessingException e) {
                log.error("[{}] 错误响应序列化失败: {}", traceId, e.getMessage(), e);
                return Mono.error(e);
            }
        });
    }

    /**
     * 将异常映射到对应的错误码。
     */
    private ResultCode mapException(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            int statusCode = rse.getStatusCode().value();
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
                default -> statusCode >= 500 ? ResultCode.INTERNAL_ERROR : ResultCode.BAD_REQUEST;
            };
        }
        return ResultCode.INTERNAL_ERROR;
    }

    /**
     * 解析异常消息，优先使用异常自身的消息。
     */
    private String resolveMessage(Throwable ex, ResultCode resultCode) {
        if (ex instanceof ResponseStatusException rse && rse.getReason() != null) {
            return rse.getReason();
        }
        if (ex.getMessage() != null) {
            return ex.getMessage();
        }
        return resultCode.getMessage();
    }
}
