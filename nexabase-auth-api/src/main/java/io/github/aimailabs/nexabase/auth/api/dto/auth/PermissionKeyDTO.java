package io.github.aimailabs.nexabase.auth.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限标识信息。
 * <p>
 * 用于网关或业务服务校验接口级权限时，比对请求路径与方法的映射。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionKeyDTO {

    /** 权限标识（如 sys:user:add） */
    private String permissionKey;

    /** 权限类型：1-菜单, 2-按钮, 3-API 接口 */
    private Integer type;

    /** 路由路径或 API URL */
    private String path;

    /** 请求方法（API 权限用） */
    private String method;
}
