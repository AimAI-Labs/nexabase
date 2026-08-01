package io.github.aimailabs.nexabase.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 网关 JWT 全局过滤器。
 * <p>
 * 校验链路：白名单放行 → 提取 Token → 验签 → 类型校验 → jwt_version 校验 → 黑名单校验 → 透传 Header。
 * <p>
 * 校验通过后写入 {@code X-User-Id} 与 {@code X-User-Name} 透传给下游微服务，
 * 下游服务通过 {@link io.github.aimailabs.nexabase.foundation.security.UserContext} 获取当前用户。
 * <p>
 * 权限细粒度校验不在此处，由下游服务的 {@code @RequiresPermissions} 注解完成。
 */
@Slf4j
@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REDIS_VERSION_PREFIX = "auth:jwt:version:";
    private static final String REDIS_BLACKLIST_PREFIX = "auth:jwt:blacklist:";

    /**
     * Redis 命令单次调用超时上限。
     * <p>
     * 独立于 {@code spring.data.redis.timeout} 的全局配置，针对网关鉴权场景收紧到 500ms，
     * 避免单次 Redis 抖动长时间阻塞请求线程，影响网关吞吐。
     */
    private static final Duration REDIS_OP_TIMEOUT = Duration.ofMillis(500);

    private final JwtVerifier jwtVerifier;
    private final JwtProperties properties;
    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public JwtGlobalFilter(JwtVerifier jwtVerifier, JwtProperties properties,
                           ReactiveStringRedisTemplate redis, ObjectMapper objectMapper) {
        this.jwtVerifier = jwtVerifier;
        this.properties = properties;
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 提取 Token
        String authHeader = exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, ResultCode.TOKEN_INVALID, "缺少认证令牌");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 验签
        Claims claims;
        try {
            claims = jwtVerifier.parseAndVerify(token);
        } catch (Exception e) {
            return unauthorized(exchange, ResultCode.TOKEN_EXPIRED, "令牌无效或已过期");
        }

        // 类型校验
        if (!"access".equals(jwtVerifier.getTokenType(claims))) {
            return unauthorized(exchange, ResultCode.TOKEN_INVALID, "令牌类型错误");
        }

        Long userId = jwtVerifier.getUserId(claims);
        Long jwtVersion = jwtVerifier.getJwtVersion(claims);
        String username = jwtVerifier.getUsername(claims);
        String jti = jwtVerifier.getJti(claims);

        // jwt_version 校验
        // Redis 不可用或超时时降级放行：JWT 验签已通过，version/blacklist 仅用于强制下线场景，
        // 牺牲该弱安全保证换取网关在 Redis 抖动时的高可用，避免鉴权链路雪崩。
        String versionKey = REDIS_VERSION_PREFIX + userId;
        return redis.opsForValue().get(versionKey)
                .timeout(REDIS_OP_TIMEOUT)
                .defaultIfEmpty("")
                .onErrorResume(ex -> {
                    log.warn("[{}] Redis jwt_version 校验降级放行: userId={}", userId, ex.getMessage());
                    return Mono.just("");
                })
                .flatMap(cachedVersion -> {
                    if (!cachedVersion.isEmpty() && !cachedVersion.equals(String.valueOf(jwtVersion))) {
                        return unauthorized(exchange, ResultCode.TOKEN_INVALID, "账号已失效，请重新登录");
                    }
                    // 黑名单校验
                    return redis.hasKey(REDIS_BLACKLIST_PREFIX + jti)
                            .timeout(REDIS_OP_TIMEOUT)
                            .onErrorResume(ex -> {
                                log.warn("[{}] Redis 黑名单校验降级放行: jti={}", jti, ex.getMessage());
                                return Mono.just(Boolean.FALSE);
                            })
                            .flatMap(blacklisted -> {
                                if (Boolean.TRUE.equals(blacklisted)) {
                                    return unauthorized(exchange, ResultCode.TOKEN_INVALID, "令牌已注销");
                                }
                                // 透传 Header
                                ServerHttpRequest mutated = exchange.getRequest().mutate()
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-User-Name", username != null ? username : "")
                                        .build();
                                return chain.filter(exchange.mutate().request(mutated).build());
                            });
                });
    }

    private boolean isWhitelisted(String path) {
        if (path == null) {
            return false;
        }
        return properties.getWhitelistPaths().stream().anyMatch(path::contains);
    }

    /**
     * 返回 401 统一错误响应。
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, ResultCode code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Result<Void> result = Result.error(code, message);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
