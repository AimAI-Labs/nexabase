package io.github.aimailabs.nexabase.auth.api.dto.assign;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 为角色分配权限请求（全量覆盖角色拥有的权限树节点）。
 */
@Data
public class AssignPermissionsRequest {

    @NotNull(message = "角色 ID 不能为空")
    private Long roleId;

    @NotEmpty(message = "权限 ID 列表不能为空")
    private List<Long> permissionIds;
}
