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
 * 角色管理接口。
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @PostMapping
    @RequiresPermissions("sys:role:add")
    @Log(module = "角色管理", action = "新增角色")
    public Result<Long> create(@Valid @RequestBody RoleCreateRequest request) {
        return Result.success(roleService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresPermissions("sys:role:edit")
    @Log(module = "角色管理", action = "编辑角色")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:role:delete")
    @Log(module = "角色管理", action = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping
    @RequiresPermissions("sys:role:list")
    public Result<List<RoleDTO>> list() {
        return Result.success(roleService.list());
    }

    @PutMapping("/{id}/permissions")
    @RequiresPermissions("sys:role:assignPermissions")
    @Log(module = "角色管理", action = "分配权限")
    public Result<Void> assignPermissions(@PathVariable Long id,
                                          @Valid @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request.getPermissionIds());
        return Result.success();
    }

    @PutMapping("/{id}/users")
    @RequiresPermissions("sys:role:assignPermissions")
    @Log(module = "角色管理", action = "分配用户")
    public Result<Void> assignToUsers(@PathVariable Long id,
                                      @RequestBody List<Long> userIds) {
        roleService.assignToUsers(id, userIds);
        return Result.success();
    }
}
