package io.github.aimailabs.nexabase.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT 验签工具（网关侧）。
 * <p>
 * 仅负责验签与 Claims 提取，不签发 Token（签发由 auth 服务负责）。
 */
public class JwtVerifier {

    private final SecretKey key;

    public JwtVerifier(JwtProperties properties) {
        byte[] keyBytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret 长度不足 32 字节，当前: " + keyBytes.length);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseAndVerify(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

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

    public String getTokenType(Claims claims) {
        return claims.get("type", String.class);
    }

    public String getJti(Claims claims) {
        return claims.getId();
    }
}
