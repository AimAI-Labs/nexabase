package io.github.aimailabs.nexabase.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.auth.api.dto.assign.AssignRolesRequest;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserCreateRequest;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserDTO;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserPageQuery;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserUpdateRequest;
import io.github.aimailabs.nexabase.auth.service.SysUserService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口。
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @PostMapping
    @RequiresPermissions("sys:user:add")
    @Log(module = "用户管理", action = "新增用户")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresPermissions("sys:user:edit")
    @Log(module = "用户管理", action = "编辑用户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:user:delete")
    @Log(module = "用户管理", action = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @RequiresPermissions("sys:user:list")
    public Result<UserDTO> get(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping
    @RequiresPermissions("sys:user:list")
    public Result<Page<UserDTO>> page(UserPageQuery query) {
        return Result.success(userService.page(query));
    }

    @PutMapping("/{id}/password/reset")
    @RequiresPermissions("sys:user:resetPassword")
    @Log(module = "用户管理", action = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequiresPermissions("sys:user:edit")
    @Log(module = "用户管理", action = "切换账号状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.toggleStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @RequiresPermissions("sys:user:assignRoles")
    @Log(module = "用户管理", action = "分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request.getRoleIds());
        return Result.success();
    }
}
