package io.github.aimailabs.nexabase.foundation.trace;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;

/**
 * RabbitMQ 消费者 Advice：在 @RabbitListener 方法执行前从 AMQP header 提取 TraceID 写入 MDC，
 * 执行后清理 MDC，防止线程池复用导致的 TraceID 串扰。
 * <p>通过 Advice 注入到 SimpleRabbitListenerContainerFactory。
 */
public class MqTraceConsumerAdvice implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Message message = extractMessage(invocation);
        String traceId = null;
        if (message != null) {
            Object header = message.getMessageProperties().getHeader(MqTraceConstants.TRACE_ID_HEADER);
            if (header != null) {
                traceId = header.toString();
            }
        }
        if (traceId == null || traceId.isBlank()) {
            traceId = TraceContext.generateTraceId();
        }
        TraceContext.setTraceId(traceId);
        try {
            return invocation.proceed();
        } finally {
            TraceContext.clear();
        }
    }

    private Message extractMessage(MethodInvocation invocation) {
        for (Object arg : invocation.getArguments()) {
            if (arg instanceof Message m) {
                return m;
            }
        }
        return null;
    }
}
