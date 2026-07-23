package io.github.aimailabs.nexabase.auth.controller;

import io.github.aimailabs.nexabase.auth.api.dto.permission.PermissionDTO;
import io.github.aimailabs.nexabase.auth.entity.SysPermission;
import io.github.aimailabs.nexabase.auth.service.SysPermissionService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.logging.Log;
import io.github.aimailabs.nexabase.foundation.security.RequiresPermissions;
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
 * 系统管理 / 权限管理接口
 * <p>
 * 提供权限资源的树形查询与增删改。
 *
 * @module nexabase-auth
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    /**
     * 查询权限树形结构（支持无限层级嵌套）。
     *
     * @return 权限树
     */
    @GetMapping("/tree")
    @RequiresPermissions("sys:permission:list")
    public Result<List<PermissionDTO>> tree() {
        return Result.success(permissionService.getTree());
    }

    /**
     * 新增权限资源节点，可指定父级权限以挂载到树结构。
     *
     * @param permission 权限实体（名称、标识 code、类型、父级 ID、路径等）
     * @return 创建成功的权限 ID
     */
    @PostMapping
    @RequiresPermissions("sys:permission:add")
    @Log(module = "权限管理", action = "新增权限")
    public Result<Long> create(@RequestBody SysPermission permission) {
        return Result.success(permissionService.create(permission));
    }

    /**
     * 更新权限资源节点。
     *
     * @param id         权限 ID
     * @param permission 权限实体
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @RequiresPermissions("sys:permission:edit")
    @Log(module = "权限管理", action = "编辑权限")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permissionService.update(id, permission);
        return Result.success();
    }

    /**
     * 删除权限资源节点（逻辑删除）。
     *
     * @param id 权限 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:permission:delete")
    @Log(module = "权限管理", action = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }
}
