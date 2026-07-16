package io.github.aimailabs.nexabase.foundation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.web.reactive.NexabaseErrorWebExceptionHandler;
import io.github.aimailabs.nexabase.foundation.web.reactive.ReactiveGlobalExceptionHandler;
import io.github.aimailabs.nexabase.foundation.web.reactive.ReactiveRequestLogFilter;
import io.github.aimailabs.nexabase.foundation.web.reactive.ReactiveTraceFilter;
import io.micrometer.context.ContextRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.DispatcherHandler;
import reactor.core.publisher.Hooks;

/**
 * Reactive（Spring WebFlux）栈自动配置。
 * <p>
 * 当模块 classpath 存在 {@link DispatcherHandler} 且应用类型为 REACTIVE 时自动激活，
 * 注册链路追踪过滤器、请求日志过滤器、全局异常处理器与 WebFlux 错误处理器。
 * <p>
 * 同时启用 Reactor Context Propagation，将 Reactor Context 中的 TraceID 自动桥接到 MDC，
 * 使响应式链路中所有日志自动携带 TraceID。
 * <p>
 * 引入 nexabase-foundation 依赖的 WebFlux 模块无需任何额外配置即可自动生效。
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(DispatcherHandler.class)
public class ReactiveWebAutoConfiguration {

    static {
        // 启用 Reactor 自动上下文传播，必须在任何响应式操作之前调用。
        // 配合下方注册的 ThreadLocalAccessor，使 Reactor Context 中的 traceId
        // 自动传播到 MDC，让所有操作符中的日志都能输出 TraceID。
        Hooks.enableAutomaticContextPropagation();
    }

    /**
     * 注册 TraceID 的 ThreadLocalAccessor，桥接 Reactor Context 与 MDC。
     * <p>
     * 当 Reactor Context 中存在 traceId 时，自动写入 MDC；
     * 当 Context 离开作用域时，自动从 MDC 移除。
     * 这样所有 Reactor 操作符中的日志都能通过 logback 模式 %X{traceId} 输出 TraceID。
     */
    @Bean
    public ContextRegistry nexabaseTraceContextRegistry() {
        ContextRegistry registry = ContextRegistry.getInstance();
        registry.registerThreadLocalAccessor(
                TraceConstants.TRACE_ID_CONTEXT_KEY,
                () -> MDC.get(TraceConstants.TRACE_ID_MDC_KEY),
                traceId -> MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId),
                () -> MDC.remove(TraceConstants.TRACE_ID_MDC_KEY)
        );
        log.info("Nexabase TraceID Context Propagation 已注册");
        return registry;
    }

    /**
     * 注册响应式链路追踪过滤器，以最高优先级运行。
     */
    @Bean
    public ReactiveTraceFilter reactiveTraceFilter() {
        return new ReactiveTraceFilter();
    }

    /**
     * 注册响应式请求访问日志过滤器，在 TraceFilter 之后执行。
     */
    @Bean
    public ReactiveRequestLogFilter reactiveRequestLogFilter() {
        return new ReactiveRequestLogFilter();
    }

    /**
     * 注册响应式全局异常处理器，拦截 Controller 层异常。
     */
    @Bean
    public ReactiveGlobalExceptionHandler reactiveGlobalExceptionHandler() {
        return new ReactiveGlobalExceptionHandler();
    }

    /**
     * 注册 WebFlux 错误处理器，处理 Controller 层之外的异常（如路由 404、代理失败）。
     * <p>
     * 以最高优先级运行，在 Spring Boot 默认的 DefaultErrorWebExceptionHandler 之前处理，
     * 确保所有错误响应统一为 Result 格式并携带 TraceID。
     */
    @Bean
    public NexabaseErrorWebExceptionHandler nexabaseErrorWebExceptionHandler(ObjectMapper objectMapper) {
        return new NexabaseErrorWebExceptionHandler(objectMapper);
    }
}
