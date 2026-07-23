package io.github.aimailabs.nexabase.auth.controller;

import io.github.aimailabs.nexabase.auth.api.dto.assign.AssignPermissionsRequest;
import io.github.aimailabs.nexabase.auth.api.dto.role.RoleCreateRequest;
import io.github.aimailabs.nexabase.auth.api.dto.role.RoleDTO;
import io.github.aimailabs.nexabase.auth.api.dto.role.RoleUpdateRequest;
import io.github.aimailabs.nexabase.auth.service.SysRoleService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.logging.Log;
import io.github.aimailabs.nexabase.foundation.security.RequiresPermissions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统管理 / 角色管理接口
 * <p>
 * 提供角色的增删改查、权限分配与用户分配。
 *
 * @module nexabase-auth
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    /**
     * 新增角色。
     *
     * @param request 新增角色请求体（角色编码、名称、描述）
     * @return 创建成功的角色 ID
     */
    @PostMapping
    @RequiresPermissions("sys:role:add")
    @Log(module = "角色管理", action = "新增角色")
    public Result<Long> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(roleService.create(request));
    }

    /**
     * 更新角色基本信息。
     *
     * @param id      角色 ID
     * @param request 更新请求体
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @RequiresPermissions("sys:role:edit")
    @Log(module = "角色管理", action = "编辑角色")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    /**
     * 删除角色（逻辑删除）。
     *
     * @param id 角色 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:role:delete")
    @Log(module = "角色管理", action = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    /**
     * 查询角色列表（扁平结构）。
     *
     * @return 角色列表
     */
    @GetMapping
    @RequiresPermissions("sys:role:list")
    public Result<List<RoleDTO>> list() {
        return Result.success(roleService.list());
    }

    /**
     * 为角色分配权限（全量覆盖式分配）。
     *
     * @param id      角色 ID
     * @param request 权限分配请求体，包含权限 ID 列表
     * @return 成功响应
     */
    @PutMapping("/{id}/permissions")
    @RequiresPermissions("sys:role:assignPermissions")
    @Log(module = "角色管理", action = "分配权限")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request.getPermissionIds());
        return Result.success();
    }

    /**
     * 为角色批量分配用户（全量覆盖式分配）。
     *
     * @param id      角色 ID
     * @param userIds 用户 ID 列表
     * @return 成功响应
     */
    @PutMapping("/{id}/users")
    @RequiresPermissions("sys:role:assignPermissions")
    @Log(module = "角色管理", action = "分配用户")
    public Result<Void> assignToUsers(@PathVariable Long id,
                                      @RequestBody List<Long> userIds) {
        roleService.assignToUsers(id, userIds);
        return Result.success();
    }
}
