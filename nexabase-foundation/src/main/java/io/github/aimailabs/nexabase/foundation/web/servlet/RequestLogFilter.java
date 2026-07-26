package io.github.aimailabs.nexabase.foundation.web.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Servlet 请求访问日志过滤器。
 * <p>
 * 记录每个 HTTP 请求的方法、URI（含参数）、客户端 IP、用户 ID、响应状态码与耗时，
 * 日志中自动携带 TraceID（由 {@link TraceFilter} 写入 MDC）。
 * 此外还会打印 Authorization 字段以及文本类的请求 Body。
 * <p>
 * 输出示例：
 * <pre>
 * 260716 | 10:30:45.123 [http-nio-8008-exec-1] [a1b2c3d4...] INFO  RequestLogFilter - → [192.168.1.10] [10001] POST /graph/persons?name=test → 200 (45ms) | Body: {"name":"test"} | Auth:[Bearer xxx]
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
        String queryString = request.getQueryString();
        String fullUri = queryString != null ? requestUri + "?" + queryString : requestUri;
        String clientIp = getClientIp(request);
        String userId = request.getHeader("X-User-Id");
        String displayUserId = (userId != null && !userId.isBlank()) ? userId : "-";

        String authHeader = request.getHeader("Authorization");
        String displayAuth = (authHeader != null && !authHeader.isBlank()) ? authHeader : "-";

        // 判断是否需要缓存 Body（仅处理文本或 JSON 类型，避免缓存大文件/二进制流）
        String contentType = request.getContentType();
        boolean shouldCacheBody = contentType != null && 
                (contentType.contains("application/json") || 
                 contentType.contains("text/") || 
                 contentType.contains("application/x-www-form-urlencoded"));

        HttpServletRequest requestToUse = request;
        if (shouldCacheBody && !(request instanceof ContentCachingRequestWrapper)) {
            requestToUse = new ContentCachingRequestWrapper(request);
        }

        HttpServletResponse responseToUse = response;
        if (!(response instanceof ContentCachingResponseWrapper)) {
            responseToUse = new ContentCachingResponseWrapper(response);
        }

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = responseToUse.getStatus();
            
            String requestBody = "";
            if (requestToUse instanceof ContentCachingRequestWrapper wrapper) {
                byte[] buf = wrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    int length = Math.min(buf.length, 1024); // 限制打印长度避免刷屏
                    String payload = new String(buf, 0, length, StandardCharsets.UTF_8);
                    requestBody = " | ReqBody: " + payload.replaceAll("\\r\\n|\\r|\\n", " ");
                    if (buf.length > 1024) {
                        requestBody += "...(truncated)";
                    }
                }
            }

            String responseBody = "";
            if (responseToUse instanceof ContentCachingResponseWrapper responseWrapper) {
                byte[] buf = responseWrapper.getContentAsByteArray();
                if (buf.length > 0) {
                    String resContentType = responseWrapper.getContentType();
                    boolean isBinary = resContentType != null &&
                            (resContentType.contains("image/") ||
                             resContentType.contains("video/") ||
                             resContentType.contains("audio/") ||
                             resContentType.contains("application/octet-stream") ||
                             resContentType.contains("application/pdf") ||
                             resContentType.contains("application/zip"));
                    if (!isBinary) {
                        int length = Math.min(buf.length, 1024); // 限制打印长度
                        String payload = new String(buf, 0, length, StandardCharsets.UTF_8);
                        responseBody = " | ResBody: " + payload.replaceAll("\\r\\n|\\r|\\n", " ");
                        if (buf.length > 1024) {
                            responseBody += "...(truncated)";
                        }
                    } else {
                        responseBody = " | ResBody: [<binary data " + buf.length + " bytes>]";
                    }
                }
                // 必须将缓存的响应体刷回原始 HttpServletResponse
                responseWrapper.copyBodyToResponse();
            }
            
            log.info("→ [{}] [{}] {} {} → {} ({}ms){}{} | Auth:[{}]", clientIp, displayUserId, method, fullUri, status, duration, requestBody, responseBody, displayAuth);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
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
