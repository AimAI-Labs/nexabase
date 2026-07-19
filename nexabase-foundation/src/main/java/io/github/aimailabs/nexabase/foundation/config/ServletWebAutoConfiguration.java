package io.github.aimailabs.nexabase.foundation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.foundation.logging.OperLogAspect;
import io.github.aimailabs.nexabase.foundation.logging.OperLogRecorder;
import io.github.aimailabs.nexabase.foundation.security.PermissionAspect;
import io.github.aimailabs.nexabase.foundation.security.PermissionChecker;
import io.github.aimailabs.nexabase.foundation.security.ServletUserContextFilter;
import io.github.aimailabs.nexabase.foundation.web.servlet.NexabaseErrorController;
import io.github.aimailabs.nexabase.foundation.web.servlet.RequestLogFilter;
import io.github.aimailabs.nexabase.foundation.web.servlet.ServletGlobalExceptionHandler;
import io.github.aimailabs.nexabase.foundation.web.servlet.TraceFilter;
import org.springframework.beans.factory.ObjectProvider;
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
 *   <li>{@link ServletUserContextFilter} — 用户上下文过滤器（从 Header 解析用户信息）</li>
 *   <li>{@link ServletGlobalExceptionHandler} — 全局异常处理器（{@code @RestControllerAdvice}）</li>
 *   <li>{@link NexabaseErrorController} — 错误控制器（处理 404 等非 Controller 异常）</li>
 *   <li>{@link PermissionAspect} — 权限校验切面（拦截 {@code @RequiresPermissions}/{@code @RequiresRoles}）</li>
 *   <li>{@link OperLogAspect} — 操作日志切面（拦截 {@code @Log}）</li>
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
     * 注册用户上下文过滤器，在 TraceFilter 之后执行，从网关透传的 Header 解析用户信息。
     */
    @Bean
    public FilterRegistrationBean<ServletUserContextFilter> userContextFilterRegistration() {
        FilterRegistrationBean<ServletUserContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ServletUserContextFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("userContextFilter");
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

    /**
     * 注册权限校验切面，拦截 {@code @RequiresPermissions} 与 {@code @RequiresRoles}。
     * <p>
     * {@link PermissionChecker} 为可选依赖，未注入时切面放行并输出 WARN。
     */
    @Bean
    public PermissionAspect permissionAspect(ObjectProvider<PermissionChecker> permissionCheckerProvider) {
        return new PermissionAspect(permissionCheckerProvider);
    }

    /**
     * 注册操作日志切面，拦截 {@code @Log} 注解采集操作信息。
     * <p>
     * {@link OperLogRecorder} 为可选依赖，未注入时切面仅输出 WARN。
     */
    @Bean
    public OperLogAspect operLogAspect(ObjectProvider<OperLogRecorder> recorderProvider,
                                       ObjectMapper objectMapper) {
        return new OperLogAspect(recorderProvider, objectMapper);
    }
}
