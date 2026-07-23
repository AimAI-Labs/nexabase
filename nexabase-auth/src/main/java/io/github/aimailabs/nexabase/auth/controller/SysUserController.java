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
 * 系统管理 / 用户管理接口
 * <p>
 * 提供用户的增删改查、分页检索、密码重置、状态切换与角色分配。
 *
 * @module nexabase-auth
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    /**
     * 新增用户，并可选分配角色与团队。
     *
     * @param request 新增用户请求体（用户名、密码、昵称、邮箱、手机号、角色 ID、团队 ID）
     * @return 创建成功的用户 ID
     */
    @PostMapping
    @RequiresPermissions("sys:user:add")
    @Log(module = "用户管理", action = "新增用户")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        return Result.success(userService.create(request));
    }

    /**
     * 更新用户基本信息（用户名、昵称、邮箱、手机号等）。
     *
     * @param id      用户 ID
     * @param request 更新请求体
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @RequiresPermissions("sys:user:edit")
    @Log(module = "用户管理", action = "编辑用户")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.update(id, request);
        return Result.success();
    }

    /**
     * 删除用户（逻辑删除）。
     *
     * @param id 用户 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:user:delete")
    @Log(module = "用户管理", action = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * 根据用户 ID 查询用户详情。
     *
     * @param id 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @RequiresPermissions("sys:user:list")
    public Result<UserDTO> get(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 分页查询用户列表，支持按用户名模糊匹配、状态及团队筛选。
     *
     * @param query 分页查询条件（用户名、状态、团队 ID、页码、每页条数）
     * @return 分页后的用户列表
     */
    @GetMapping
    @RequiresPermissions("sys:user:list")
    public Result<Page<UserDTO>> page(UserPageQuery query) {
        return Result.success(userService.page(query));
    }

    /**
     * 重置指定用户的登录密码。
     *
     * @param id 用户 ID
     * @return 成功响应
     */
    @PutMapping("/{id}/password/reset")
    @RequiresPermissions("sys:user:resetPassword")
    @Log(module = "用户管理", action = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }

    /**
     * 切换指定用户的状态（启用/禁用）。
     *
     * @param id     用户 ID
     * @param status 目标状态：1-正常, 0-禁用
     * @return 成功响应
     */
    @PutMapping("/{id}/status")
    @RequiresPermissions("sys:user:edit")
    @Log(module = "用户管理", action = "切换账号状态")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.toggleStatus(id, status);
        return Result.success();
    }

    /**
     * 为用户分配角色（全量覆盖式分配）。
     *
     * @param id      用户 ID
     * @param request 角色分配请求体，包含角色 ID 列表
     * @return 成功响应
     */
    @PutMapping("/{id}/roles")
    @RequiresPermissions("sys:user:assignRoles")
    @Log(module = "用户管理", action = "分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request.getRoleIds());
        return Result.success();
    }
}
