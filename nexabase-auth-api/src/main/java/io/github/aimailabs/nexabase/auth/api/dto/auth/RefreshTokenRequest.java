package io.github.aimailabs.nexabase.auth.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求。
 */
@Data
public class RefreshTokenRequest {

    /** Refresh Token */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
