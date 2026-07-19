package io.github.aimailabs.nexabase.auth.api.security;

import io.github.aimailabs.nexabase.auth.api.feign.AuthFeignClient;
import io.github.aimailabs.nexabase.auth.api.feign.AuthFeignClientFallback;
import io.github.aimailabs.nexabase.foundation.security.PermissionChecker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * auth-api 自动配置。
 * <p>
 * 当 classpath 存在 {@link StringRedisTemplate} 与 {@link io.github.aimailabs.nexabase.foundation.security.PermissionChecker}
 * 时自动激活，注册：
 * <ul>
 *   <li>{@link AuthFeignClient} — Feign 客户端（通过 {@link EnableFeignClients} 扫描）</li>
 *   <li>{@link AuthFeignClientFallback} — 降级实现</li>
 *   <li>{@link AuthFeignRequestInterceptor} — Header 透传拦截器</li>
 *   <li>{@link RedisPermissionChecker} — 权限校验默认实现（可被业务覆盖）</li>
 * </ul>
 * 业务服务引入 {@code nexabase-auth-api} 依赖后自动生效，无需额外配置。
 */
@AutoConfiguration
@ConditionalOnClass({StringRedisTemplate.class, AuthFeignClient.class})
@EnableFeignClients(basePackages = "io.github.aimailabs.nexabase.auth.api.feign")
public class AuthApiAutoConfiguration {

    /**
     * 注册 Feign 降级实现。
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthFeignClientFallback authFeignClientFallback() {
        return new AuthFeignClientFallback();
    }

    /**
     * 注册 Feign 请求拦截器，透传链路与用户 Header。
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthFeignRequestInterceptor authFeignRequestInterceptor() {
        return new AuthFeignRequestInterceptor();
    }

    /**
     * 注册权限校验默认实现。
     * <p>
     * 业务服务可通过自定义 {@link PermissionChecker} Bean 覆盖此实现。
     */
    @Bean
    @ConditionalOnMissingBean(PermissionChecker.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    public PermissionChecker redisPermissionChecker(StringRedisTemplate redisTemplate,
                                                    AuthFeignClient authFeignClient) {
        return new RedisPermissionChecker(redisTemplate, authFeignClient);
    }
}
