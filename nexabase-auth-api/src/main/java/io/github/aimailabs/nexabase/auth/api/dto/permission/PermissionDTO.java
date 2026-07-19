package io.github.aimailabs.nexabase.auth.api.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限资源视图（树形结构）。
 * <p>
 * 通过 {@code children} 字段表达父子层级，前端据此渲染权限树。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private Long id;
    private Long parentId;
    private String name;

    /** 权限类型：1-菜单, 2-按钮, 3-API 接口 */
    private Integer type;

    /** 权限标识（如 sys:user:add） */
    private String permissionKey;

    /** 路由路径或 API URL */
    private String path;

    /** 请求方法（API 权限用） */
    private String method;

    /** 前端组件路径（菜单权限用） */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;

    /** 子权限列表 */
    @Builder.Default
    private List<PermissionDTO> children = new ArrayList<>();
}
