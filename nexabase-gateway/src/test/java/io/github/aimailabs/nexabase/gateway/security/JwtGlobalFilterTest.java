package io.github.aimailabs.nexabase.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtGlobalFilterTest {

    @Mock
    private JwtVerifier jwtVerifier;

    @Mock
    private ReactiveStringRedisTemplate redis;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtProperties properties;
    private ObjectMapper objectMapper;
    private JwtGlobalFilter filter;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setWhitelistPaths(List.of("/api/v1/auth/login", "/doc.html"));
        objectMapper = new ObjectMapper();
        filter = new JwtGlobalFilter(jwtVerifier, properties, redis, objectMapper);
    }

    @Test
    @DisplayName("命中白名单路径时直接放行")
    void filter_whenWhitelistedPath_shouldPassDirectly() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(exchange);
        verifyNoInteractions(jwtVerifier, redis);
    }

    @Test
    @DisplayName("缺少 Authorization 头时返回 401")
    void filter_whenMissingAuthHeader_shouldReturnUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/document/list").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(filterChain, jwtVerifier, redis);
    }

    @Test
    @DisplayName("开发测试 Token 开启且 Token 匹配时，直接放行并注入 X-User-Id / X-User-Name，跳过 JWT 验签与 Redis 校验")
    void filter_whenDevTokenEnabledAndMatches_shouldInjectHeadersAndBypassJwt() {
        properties.getDevToken().setEnabled(true);
        properties.getDevToken().setToken("dev-test-token");
        properties.getDevToken().setUserId(99L);
        properties.getDevToken().setUsername("test-developer");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/document/list")
                .header("Authorization", "Bearer dev-test-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        when(filterChain.filter(any())).thenAnswer(invocation -> {
            capturedExchange.set(invocation.getArgument(0));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        verify(filterChain).filter(any());
        verifyNoInteractions(jwtVerifier, redis);

        assertThat(capturedExchange.get()).isNotNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("99");
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Name")).isEqualTo("test-developer");
    }

    @Test
    @DisplayName("开发测试 Token 关闭时，携带 dev token 走正常 JWT 流程，验签失败返回 401")
    void filter_whenDevTokenDisabled_shouldPerformJwtVerificationAndFail() {
        properties.getDevToken().setEnabled(false);
        properties.getDevToken().setToken("dev-test-token");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/document/list")
                .header("Authorization", "Bearer dev-test-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtVerifier.parseAndVerify("dev-test-token")).thenThrow(new RuntimeException("Invalid JWT"));

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(jwtVerifier).parseAndVerify("dev-test-token");
        verifyNoInteractions(filterChain, redis);
    }

    @Test
    @DisplayName("正常合法 JWT Token 校验通过后放行并注入 Header")
    void filter_whenValidJwt_shouldPassAndInjectHeaders() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/document/list")
                .header("Authorization", "Bearer valid.jwt.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Claims mockClaims = mock(Claims.class);
        when(jwtVerifier.parseAndVerify("valid.jwt.token")).thenReturn(mockClaims);
        when(jwtVerifier.getTokenType(mockClaims)).thenReturn("access");
        when(jwtVerifier.getUserId(mockClaims)).thenReturn(100L);
        when(jwtVerifier.getJwtVersion(mockClaims)).thenReturn(1L);
        when(jwtVerifier.getUsername(mockClaims)).thenReturn("alice");
        when(jwtVerifier.getJti(mockClaims)).thenReturn("jti-123");

        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:jwt:version:100")).thenReturn(Mono.just("1"));
        when(redis.hasKey("auth:jwt:blacklist:jti-123")).thenReturn(Mono.just(Boolean.FALSE));

        AtomicReference<ServerWebExchange> capturedExchange = new AtomicReference<>();
        when(filterChain.filter(any())).thenAnswer(invocation -> {
            capturedExchange.set(invocation.getArgument(0));
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(capturedExchange.get()).isNotNull();
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Id")).isEqualTo("100");
        assertThat(capturedExchange.get().getRequest().getHeaders().getFirst("X-User-Name")).isEqualTo("alice");
    }
}
