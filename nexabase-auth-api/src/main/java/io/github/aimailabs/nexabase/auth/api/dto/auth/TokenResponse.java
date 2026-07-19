package io.github.aimailabs.nexabase.auth.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录/刷新成功后返回的双 Token 响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /** 短期访问令牌（30 分钟） */
    private String accessToken;

    /** 长期刷新令牌（7 天） */
    private String refreshToken;

    /** Access Token 有效期（秒） */
    private Long expiresIn;

    /** 令牌类型，固定为 Bearer */
    @Builder.Default
    private String tokenType = "Bearer";
}
