package io.github.aimailabs.nexabase.foundation.security;

import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 用户上下文过滤器。
 * <p>
 * 在 {@link io.github.aimailabs.nexabase.foundation.web.servlet.TraceFilter} 之后执行，
 * 从网关透传的 HTTP Header（{@code X-User-Id} / {@code X-User-Name}）解析当前用户信息，
 * 写入 {@link UserContext}（ThreadLocal）与 MDC（供日志按用户过滤）。
 * <p>
 * 请求结束时清除 ThreadLocal，防止线程池复用导致用户上下文串扰。
 * MDC 的清除由 {@link TraceFilter} 统一负责。
 */
@Slf4j
public class ServletUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userIdStr = request.getHeader(UserContext.USER_ID_HEADER);
        String userName = request.getHeader(UserContext.USER_NAME_HEADER);

        Long userId = null;
        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("非法的 X-User-Id 头: {}", userIdStr);
            }
        }

        if (userId != null) {
            UserContext.set(userId, userName);
            // 同步写入 MDC，便于日志按用户过滤
            TraceContext.setUserId(String.valueOf(userId));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
