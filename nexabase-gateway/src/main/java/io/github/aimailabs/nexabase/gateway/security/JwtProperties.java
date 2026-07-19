package io.github.aimailabs.nexabase.gateway.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 网关 JWT 配置属性。
 * <p>
 * 密钥必须与 auth 服务一致（建议通过 Nacos 共享配置 {@code nexabase.jwt.secret}）。
 */
@Data
@ConfigurationProperties(prefix = "nexabase.jwt")
public class JwtProperties {

    private String secret = "change-me-in-production-at-least-32-chars";

    private Duration accessTokenTtl = Duration.ofMinutes(30);

    private Duration refreshTokenTtl = Duration.ofDays(7);

    /** 无需鉴权的白名单路径（前缀匹配） */
    private List<String> whitelistPaths = new ArrayList<>();
}
