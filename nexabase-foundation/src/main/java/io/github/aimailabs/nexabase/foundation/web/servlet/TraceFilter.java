package io.github.aimailabs.nexabase.foundation.web.servlet;

import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 链路追踪过滤器。
 * <p>
 * 在请求最前端执行，确保每个请求都有 TraceID：
 * <ol>
 *   <li>从 {@code X-Trace-Id} 请求头提取上游传递的 TraceID</li>
 *   <li>若不存在则生成新的 TraceID（网关或直连入口场景）</li>
 *   <li>写入 MDC（供 logback 日志模式 {@code %X{traceId}} 自动输出）</li>
 *   <li>写入请求属性（供 ErrorController 在 error dispatch 时复用）</li>
 *   <li>写入响应头（供调用方/前端关联请求）</li>
 *   <li>请求结束时清除 MDC，防止线程池复用导致 TraceID 串扰</li>
 * </ol>
 * <p>
 * 过滤器同时在 error dispatch 时执行（覆盖 404 等非 Controller 异常路径），
 * 从请求属性中恢复原始 TraceID，确保错误响应也携带正确的追踪标识。
 */
@Slf4j
public class TraceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 优先从请求属性获取（error dispatch 时已由初始 dispatch 设置）
        String traceId = (String) request.getAttribute(TraceConstants.TRACE_ID_HEADER);

        if (traceId == null) {
            // 其次从请求头获取（上游服务传递）
            traceId = request.getHeader(TraceConstants.TRACE_ID_HEADER);
            // 均不存在则生成新的 TraceID
            if (traceId == null || traceId.isBlank()) {
                traceId = TraceContext.generateTraceId();
            }
            // 存入请求属性，供 error dispatch 复用
            request.setAttribute(TraceConstants.TRACE_ID_HEADER, traceId);
        }

        // 写入 MDC，使当前线程所有日志自动携带 TraceID
        TraceContext.setTraceId(traceId);

        // 写入响应头，供调用方关联
        response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 必须清除 MDC，防止线程池复用导致 TraceID 串扰
            TraceContext.clear();
        }
    }

    /**
     * 在 error dispatch 时也执行此过滤器，
     * 以便从请求属性恢复 TraceID 到 MDC。
     */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
