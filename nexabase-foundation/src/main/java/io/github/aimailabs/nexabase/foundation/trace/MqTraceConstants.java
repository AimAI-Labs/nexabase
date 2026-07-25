package io.github.aimailabs.nexabase.foundation.trace;

/**
 * MQ 链路追踪常量。
 * <p>复用 HTTP 同名 header {@link TraceConstants#TRACE_ID_HEADER}，
 * 实现 HTTP → MQ → HTTP 端到端 TraceID 串联。
 */
public final class MqTraceConstants {
    private MqTraceConstants() {
    }

    /** AMQP message header key，与 HTTP 头 X-Trace-Id 一致。 */
    public static final String TRACE_ID_HEADER = TraceConstants.TRACE_ID_HEADER;
}
