package io.github.aimailabs.nexabase.foundation.trace;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * RabbitMQ 生产者消息后处理器：发送前将当前 MDC TraceID 注入 AMQP header。
 * <p>仅当 MDC 已存在 TraceID 时注入；不存在则生成新 ID，保证下游可串联。
 */
public class MqTraceMessagePostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) {
        String traceId = TraceContext.getTraceId();
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }
        message.getMessageProperties().setHeader(MqTraceConstants.TRACE_ID_HEADER, traceId);
        return message;
    }
}
