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
 * 权限资源管理接口。
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @GetMapping("/tree")
    @RequiresPermissions("sys:permission:list")
    public Result<List<PermissionDTO>> tree() {
        return Result.success(permissionService.getTree());
    }

    @PostMapping
    @RequiresPermissions("sys:permission:add")
    @Log(module = "权限管理", action = "新增权限")
    public Result<Long> create(@RequestBody SysPermission permission) {
        return Result.success(permissionService.create(permission));
    }

    @PutMapping("/{id}")
    @RequiresPermissions("sys:permission:edit")
    @Log(module = "权限管理", action = "编辑权限")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysPermission permission) {
        permissionService.update(id, permission);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:permission:delete")
    @Log(module = "权限管理", action = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success();
    }
}
