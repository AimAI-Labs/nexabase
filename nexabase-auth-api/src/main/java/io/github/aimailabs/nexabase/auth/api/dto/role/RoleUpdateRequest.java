package io.github.aimailabs.nexabase.auth.api.dto.role;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改角色请求（不允许修改 code）。
 */
@Data
public class RoleUpdateRequest {

    @Size(max = 64, message = "角色名称长度不能超过 64")
    private String name;

    @Size(max = 255, message = "角色描述长度不能超过 255")
    private String description;

    /** 状态：1-正常, 0-停用 */
    private Integer status;
}
