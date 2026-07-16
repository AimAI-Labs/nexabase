package io.github.aimailabs.nexabase.foundation.trace;

/**
 * 链路追踪常量定义。
 * <p>
 * 统一管理 TraceID 相关的 HTTP 头名称、MDC 键名与 Reactor Context 键名，
 * 确保所有模块使用一致的标识，实现跨服务链路串联。
 */
public final class TraceConstants {

    private TraceConstants() {
    }

    /**
     * TraceID 的 HTTP 请求/响应头名称。
     * <p>
     * 网关生成 TraceID 后通过此头向下游服务传递；
     * 下游服务从此头读取或生成新的 TraceID，并通过响应头返回给调用方。
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC（Mapped Diagnostic Context）中存储 TraceID 的键名。
     * <p>
     * logback 日志模式通过 {@code %X{traceId}} 引用此键，
     * 使每条日志自动携带 TraceID，实现日志与异常的上下文关联。
     */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    /**
     * Reactor Context 中存储 TraceID 的键名。
     * <p>
     * 用于响应式（WebFlux）场景下跨线程传递 TraceID，
     * 通过 Micrometer Context Propagation 自动桥接到 MDC。
     */
    public static final String TRACE_ID_CONTEXT_KEY = "traceId";

    /**
     * SpanID 的 HTTP 头名称（预留，用于更细粒度的请求分段追踪）。
     */
    public static final String SPAN_ID_HEADER = "X-Span-Id";

    /**
     * MDC 中存储 SpanID 的键名。
     */
    public static final String SPAN_ID_MDC_KEY = "spanId";

    /**
     * MDC 中存储用户 ID 的键名（由认证模块写入，便于按用户过滤日志）。
     */
    public static final String USER_ID_MDC_KEY = "userId";
}
