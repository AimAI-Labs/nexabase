package io.github.aimailabs.nexabase.foundation.config;

import io.github.aimailabs.nexabase.foundation.web.servlet.NexabaseErrorController;
import io.github.aimailabs.nexabase.foundation.web.servlet.RequestLogFilter;
import io.github.aimailabs.nexabase.foundation.web.servlet.ServletGlobalExceptionHandler;
import io.github.aimailabs.nexabase.foundation.web.servlet.TraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Servlet（Spring MVC）栈自动配置。
 * <p>
 * 当模块 classpath 存在 {@link DispatcherServlet} 且应用类型为 SERVLET 时自动激活，
 * 注册以下组件：
 * <ul>
 *   <li>{@link TraceFilter} — 链路追踪过滤器（最高优先级）</li>
 *   <li>{@link RequestLogFilter} — 请求访问日志过滤器</li>
 *   <li>{@link ServletGlobalExceptionHandler} — 全局异常处理器（{@code @RestControllerAdvice}）</li>
 *   <li>{@link NexabaseErrorController} — 错误控制器（处理 404 等非 Controller 异常）</li>
 * </ul>
 * <p>
 * 引入 {@code nexabase-foundation} 依赖的 MVC 模块无需任何额外配置即可自动生效。
 */
@AutoConfiguration
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
public class ServletWebAutoConfiguration {

    /**
     * 注册链路追踪过滤器，以最高优先级运行，确保后续所有过滤器和 Controller 都有 TraceID。
     */
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistration() {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("traceFilter");
        return registration;
    }

    /**
     * 注册请求访问日志过滤器，在 TraceFilter 之后执行，确保日志中携带 TraceID。
     */
    @Bean
    public FilterRegistrationBean<RequestLogFilter> requestLogFilterRegistration() {
        FilterRegistrationBean<RequestLogFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLogFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.setName("requestLogFilter");
        return registration;
    }

    /**
     * 注册全局异常处理器，拦截所有 Controller 层异常。
     */
    @Bean
    public ServletGlobalExceptionHandler servletGlobalExceptionHandler() {
        return new ServletGlobalExceptionHandler();
    }

    /**
     * 注册错误控制器，处理未被 @RestControllerAdvice 拦截的错误（如 404）。
     */
    @Bean
    public NexabaseErrorController nexabaseErrorController() {
        return new NexabaseErrorController();
    }
}
