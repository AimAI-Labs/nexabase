package io.github.aimailabs.nexabase.foundation.web.reactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Reactive 请求访问日志过滤器。
 * <p>
 * 记录每个 HTTP 请求的方法、URI、响应状态码与耗时。
 * 日志中自动携带 TraceID（由 {@link ReactiveTraceFilter} 写入 Reactor Context，
 * 通过 context-propagation 自动桥接到 MDC）。
 * <p>
 * 输出示例：
 * <pre>
 * 260716 | 10:30:45.123 [reactor-http-nio-2] [a1b2c3d4...] INFO  ReactiveRequestLogFilter - → GET /test/nacos/status → 200 (12ms)
 * </pre>
 */
@Slf4j
public class ReactiveRequestLogFilter implements WebFilter, Ordered {

    private static final String START_TIME_ATTR = "nexabase.request.startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String uri = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange)
                .doOnSuccess(v -> logRequest(method, uri, getStatusCode(exchange), startTime))
                .doOnError(e -> logRequest(method, uri, getStatusCode(exchange), startTime));
    }

    private void logRequest(String method, String uri, int status, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("→ {} {} → {} ({}ms)", method, uri, status, duration);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

    private int getStatusCode(ServerWebExchange exchange) {
        HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
        return statusCode != null ? statusCode.value() : 500;
    }
}
