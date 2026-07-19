package io.github.aimailabs.nexabase.auth.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求。
 */
@Data
public class LoginRequest {

    /** 登录账号 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 登录密码（明文，传输层由 HTTPS 保护） */
    @NotBlank(message = "密码不能为空")
    private String password;
}
