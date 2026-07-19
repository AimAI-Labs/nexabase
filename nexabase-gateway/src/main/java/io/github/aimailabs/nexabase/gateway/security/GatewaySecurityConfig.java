package io.github.aimailabs.nexabase.gateway.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关安全配置。
 * <p>
 * 注册 {@link JwtVerifier} 与 {@link JwtProperties}。
 * {@link JwtGlobalFilter} 通过 {@code @Component} 自动注册。
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class GatewaySecurityConfig {

    @Bean
    public JwtVerifier jwtVerifier(JwtProperties properties) {
        return new JwtVerifier(properties);
    }
}
