package io.github.aimailabs.nexabase.foundation.web.reactive;

import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Reactive 链路追踪过滤器。
 * <p>
 * 在 WebFlux 请求最前端执行，确保每个请求都有 TraceID：
 * <ol>
 *   <li>从 {@code X-Trace-Id} 请求头提取上游传递的 TraceID</li>
 *   <li>若不存在则生成新的 TraceID</li>
 *   <li>写入请求头（供网关代理到下游服务时自动传递）</li>
 *   <li>写入响应头（供调用方关联请求）</li>
 *   <li>写入 Reactor Context（通过 Micrometer Context Propagation 自动桥接到 MDC）</li>
 * </ol>
 * <p>
 * 配合 {@link io.github.aimailabs.nexabase.foundation.config.ReactiveWebAutoConfiguration}
 * 中注册的 {@code ThreadLocalAccessor} 和 {@code Hooks.enableAutomaticContextPropagation()}，
 * 所有 Reactor 操作符中的日志都会自动携带 TraceID，无需手动管理 MDC。
 */
public class ReactiveTraceFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 从请求头获取上游传递的 TraceID
        String traceId = exchange.getRequest().getHeaders().getFirst(TraceConstants.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }

        // 将 TraceID 写入请求头（网关代理到下游服务时会自动传递请求头）
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(TraceConstants.TRACE_ID_HEADER, traceId)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        // 写入响应头，供调用方关联
        mutatedExchange.getResponse().getHeaders().add(TraceConstants.TRACE_ID_HEADER, traceId);

        // 写入 Reactor Context，context-propagation 自动桥接到 MDC
        return chain.filter(mutatedExchange)
                .contextWrite(Context.of(TraceConstants.TRACE_ID_CONTEXT_KEY, traceId));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
