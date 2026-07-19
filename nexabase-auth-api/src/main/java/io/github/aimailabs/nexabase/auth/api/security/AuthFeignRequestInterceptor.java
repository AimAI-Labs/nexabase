package io.github.aimailabs.nexabase.auth.api.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 请求拦截器。
 * <p>
 * 在 Feign 调用 auth 内部接口时，从当前 HTTP 请求上下文透传链路追踪与用户标识 Header，
 * 保证跨服务调用的 TraceID 与用户上下文完整。
 * <p>
 * 透传 Header：
 * <ul>
 *   <li>{@code X-Trace-Id} — 链路追踪 ID</li>
 *   <li>{@code X-User-Id} — 当前用户 ID</li>
 *   <li>{@code X-User-Name} — 当前用户名</li>
 * </ul>
 */
@Slf4j
public class AuthFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            // 非 HTTP 请求上下文（如定时任务发起的调用），跳过
            return;
        }
        copyHeader(request, template, TraceConstants.TRACE_ID_HEADER);
        copyHeader(request, template, UserContext.USER_ID_HEADER);
        copyHeader(request, template, UserContext.USER_NAME_HEADER);
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            template.header(headerName, value);
        }
    }
}
