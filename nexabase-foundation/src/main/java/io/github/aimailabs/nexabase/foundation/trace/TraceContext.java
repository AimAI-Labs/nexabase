package io.github.aimailabs.nexabase.foundation.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 链路追踪上下文工具类。
 * <p>
 * 封装 TraceID 的生成、获取、设置与清除操作，基于 SLF4J MDC 实现。
 * <ul>
 *   <li><b>Servlet 场景</b>：{@link io.github.aimailabs.nexabase.foundation.web.servlet.TraceFilter}
 *       在请求入口调用 {@link #setTraceId}，请求结束时调用 {@link #clear}。</li>
 *   <li><b>Reactive 场景</b>：{@link io.github.aimailabs.nexabase.foundation.web.reactive.ReactiveTraceFilter}
 *       将 TraceID 写入 Reactor Context，通过 Context Propagation 自动桥接到 MDC。</li>
 * </ul>
 * 所有日志通过 logback 模式 {@code %X{traceId}} 自动输出当前请求的 TraceID，
 * 异常处理器也通过 {@link #getTraceId()} 将 TraceID 写入错误响应，实现端到端关联。
 */
public final class TraceContext {

    private TraceContext() {
    }

    /**
     * 生成新的 TraceID。
     * <p>
     * 采用 UUID 去除连字符的形式，32 位十六进制字符串，
     * 全局唯一且无安全敏感信息泄露风险。
     *
     * @return 32 位 TraceID 字符串
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 从 MDC 获取当前 TraceID。
     *
     * @return 当前 TraceID，未设置时返回 {@code null}
     */
    public static String getTraceId() {
        return MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
    }

    /**
     * 将 TraceID 写入 MDC。
     * <p>
     * 写入后，当前线程内所有日志输出都会自动携带此 TraceID。
     *
     * @param traceId 要设置的 TraceID
     */
    public static void setTraceId(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
        }
    }

    /**
     * 从 MDC 移除 TraceID。
     * <p>
     * 必须在请求处理完成后调用，防止线程池复用导致的 TraceID 串扰。
     */
    public static void clear() {
        MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
        MDC.remove(TraceConstants.SPAN_ID_MDC_KEY);
        MDC.remove(TraceConstants.USER_ID_MDC_KEY);
    }

    /**
     * 将用户 ID 写入 MDC，便于按用户维度过滤日志。
     *
     * @param userId 用户 ID
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            MDC.put(TraceConstants.USER_ID_MDC_KEY, userId);
        }
    }
}
