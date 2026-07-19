package io.github.aimailabs.nexabase.auth.config;

import io.github.aimailabs.nexabase.auth.security.JwtProperties;
import io.github.aimailabs.nexabase.auth.security.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置。
 * <p>
 * 注册：
 * <ul>
 *   <li>{@link PasswordEncoder} — BCrypt 密码编码器</li>
 *   <li>{@link JwtTokenService} — JWT 签发与验签服务</li>
 *   <li>{@link JwtProperties} — 通过 {@link EnableConfigurationProperties} 激活</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * BCrypt 密码编码器（强度 10）。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT Token 服务。
     */
    @Bean
    public JwtTokenService jwtTokenService(JwtProperties jwtProperties) {
        return new JwtTokenService(jwtProperties);
    }
}
