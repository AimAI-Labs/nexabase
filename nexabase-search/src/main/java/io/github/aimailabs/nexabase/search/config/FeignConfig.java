package io.github.aimailabs.nexabase.search.config;

import feign.RequestInterceptor;
import io.github.aimailabs.nexabase.foundation.trace.TraceConstants;
import io.github.aimailabs.nexabase.foundation.trace.TraceContext;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 配置：扫描 document-api 内部客户端，并透传 TraceID 到下游内部调用。
 */
@Configuration
@EnableFeignClients(basePackages = "io.github.aimailabs.nexabase.document.api.client")
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
