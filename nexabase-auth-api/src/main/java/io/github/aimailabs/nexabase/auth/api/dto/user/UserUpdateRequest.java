package io.github.aimailabs.nexabase.auth.api.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改用户请求（不允许修改用户名与密码）。
 */
@Data
public class UserUpdateRequest {

    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    @Size(max = 128, message = "邮箱长度不能超过 128")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;
}
