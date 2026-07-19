package io.github.aimailabs.nexabase.auth.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 配置属性。
 * <p>
 * 通过 Nacos 或本地配置 {@code nexabase.jwt.*} 注入。
 * 密钥需至少 32 字节（HS256 要求），生产环境必须替换默认值。
 */
@Data
@ConfigurationProperties(prefix = "nexabase.jwt")
public class JwtProperties {

    /** HS256 签名密钥（至少 32 字节） */
    private String secret = "change-me-in-production-at-least-32-chars";

    /** Access Token 有效期，默认 30 分钟 */
    private Duration accessTokenTtl = Duration.ofMinutes(30);

    /** Refresh Token 有效期，默认 7 天 */
    private Duration refreshTokenTtl = Duration.ofDays(7);

    /** 网关白名单路径（无需鉴权放行） */
    private List<String> whitelistPaths = new ArrayList<>();
}
