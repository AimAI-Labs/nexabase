package io.github.aimailabs.nexabase.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token 服务。
 * <p>
 * 负责双 Token 的签发与验签：
 * <ul>
 *   <li>Access Token（短期）：携带 userId/username/jwtVersion/type=access</li>
 *   <li>Refresh Token（长期）：仅携带 userId/type=refresh，用于换取新 Access Token</li>
 * </ul>
 * 采用 HS256 对称签名，密钥由 {@link JwtProperties} 注入。
 * <p>
 * 每个 Token 携带唯一 jti（JWT ID），用于黑名单精确注销。
 */
@Slf4j
public class JwtTokenService {

    /** Access Token 类型标识 */
    public static final String TYPE_ACCESS = "access";

    /** Refresh Token 类型标识 */
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret 长度不足 32 字节，当前: " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 签发 Access Token。
     *
     * @param userId     用户 ID
     * @param username   用户名
     * @param jwtVersion JWT 版本号（用于批量踢下线）
     * @return Access Token 字符串
     */
    public String generateAccessToken(Long userId, String username, Long jwtVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getAccessTokenTtl().toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("jwtVersion", jwtVersion)
                .claim("type", TYPE_ACCESS)
                .id(UUID.randomUUID().toString().replace("-", ""))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 签发 Refresh Token。
     * <p>
     * 携带 jwtVersion 以支持踢下线：账号被禁用或踢下线时 jwt_version 自增，
     * 旧 refresh token 因版本不匹配而失效，无法换取新 access token。
     *
     * @param userId     用户 ID
     * @param jwtVersion JWT 版本号
     * @return Refresh Token 字符串
     */
    public String generateRefreshToken(Long userId, Long jwtVersion) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getRefreshTokenTtl().toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("jwtVersion", jwtVersion)
                .claim("type", TYPE_REFRESH)
                .id(UUID.randomUUID().toString().replace("-", ""))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 解析并验签 Token。
     * <p>
     * 验签失败或过期时抛出异常，由调用方捕获处理。
     *
     * @param token JWT 字符串
     * @return Claims 声明集合
     */
    public Claims parseAndVerify(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Claims 提取用户 ID。
     */
    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Claims 提取用户名。
     */
    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    /**
     * 从 Claims 提取 JWT 版本号。
     */
    public Long getJwtVersion(Claims claims) {
        Object v = claims.get("jwtVersion");
        if (v instanceof Number num) {
            return num.longValue();
        }
        if (v instanceof String str) {
            return Long.parseLong(str);
        }
        return null;
    }

    /**
     * 从 Claims 提取 Token 类型。
     */
    public String getTokenType(Claims claims) {
        return claims.get("type", String.class);
    }

    /**
     * 从 Claims 提取 jti（JWT ID，用于黑名单）。
     */
    public String getJti(Claims claims) {
        return claims.getId();
    }

    /**
     * 获取 Access Token 有效期（秒）。
     */
    public long getAccessTokenExpiresInSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }
}
