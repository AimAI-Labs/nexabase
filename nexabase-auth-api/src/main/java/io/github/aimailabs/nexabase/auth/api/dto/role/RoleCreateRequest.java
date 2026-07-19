package io.github.aimailabs.nexabase.auth.api.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增角色请求。
 */
@Data
public class RoleCreateRequest {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称长度不能超过 64")
    private String name;

    @NotBlank(message = "角色标识不能为空")
    @Size(max = 64, message = "角色标识长度不能超过 64")
    private String code;

    @Size(max = 255, message = "角色描述长度不能超过 255")
    private String description;
}
