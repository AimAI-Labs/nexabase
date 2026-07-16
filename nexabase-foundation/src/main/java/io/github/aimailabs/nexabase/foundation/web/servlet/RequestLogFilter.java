package io.github.aimailabs.nexabase.foundation.web.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 请求访问日志过滤器。
 * <p>
 * 记录每个 HTTP 请求的方法、URI、响应状态码与耗时，
 * 日志中自动携带 TraceID（由 {@link TraceFilter} 写入 MDC）。
 * <p>
 * 输出示例：
 * <pre>
 * 260716 | 10:30:45.123 [http-nio-8008-exec-1] [a1b2c3d4...] INFO  RequestLogFilter - → POST /graph/persons → 200 (45ms)
 * </pre>
 * <p>
 * 仅记录非静态资源请求，避免日志噪音。
 */
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private static final String[] STATIC_RESOURCE_PREFIXES = {
            "/favicon.ico", "/webjars/", "/swagger-ui/", "/v3/api-docs", "/doc.html"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // 跳过静态资源，避免日志噪音
        if (isStaticResource(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            log.info("→ {} {} → {} ({}ms)", method, requestUri, status, duration);
        }
    }

    private boolean isStaticResource(String uri) {
        if (uri == null) {
            return false;
        }
        for (String prefix : STATIC_RESOURCE_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
