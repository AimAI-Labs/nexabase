package io.github.aimailabs.nexabase.auth.api.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新增用户请求。
 */
@Data
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过 64")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 之间")
    private String password;

    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    @Size(max = 128, message = "邮箱长度不能超过 128")
    private String email;

    @Size(max = 20, message = "手机号长度不能超过 20")
    private String phone;

    /** 直接分配的角色 ID 列表 */
    private List<Long> roleIds;

    /** 归属团队 ID 列表 */
    private List<Long> teamIds;
}
