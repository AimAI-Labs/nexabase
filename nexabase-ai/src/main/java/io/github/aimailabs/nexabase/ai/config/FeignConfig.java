package io.github.aimailabs.nexabase.ai.config;

import feign.RequestInterceptor;
import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置：扫描 document-api 内部客户端 + ai 自身 search 调用客户端，并透传 TraceID。
 */
@Configuration
@EnableFeignClients(basePackages = {
        "io.github.aimailabs.nexabase.document.api.client",
        "io.github.aimailabs.nexabase.ai.feign"
})
public class FeignConfig {

    @Bean
    public RequestInterceptor traceIdRequestInterceptor() {
        return template -> {
            String traceId = TraceContext.getTraceId();
            if (traceId != null && !traceId.isBlank()) {
                template.header(TraceConstants.TRACE_ID_HEADER, traceId);
            }
        };
    }
}
