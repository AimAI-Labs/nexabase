package io.github.aimailabs.nexabase.auth.controller;

import io.github.aimailabs.nexabase.auth.api.dto.assign.AssignRolesRequest;
import io.github.aimailabs.nexabase.auth.api.dto.team.TeamCreateRequest;
import io.github.aimailabs.nexabase.auth.api.dto.team.TeamDTO;
import io.github.aimailabs.nexabase.auth.service.SysTeamService;
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
 * 团队管理接口。
 */
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class SysTeamController {

    private final SysTeamService teamService;

    @GetMapping("/tree")
    @RequiresPermissions("sys:team:list")
    public Result<List<TeamDTO>> tree() {
        return Result.success(teamService.getTree());
    }

    @PostMapping
    @RequiresPermissions("sys:team:add")
    @Log(module = "团队管理", action = "新增团队")
    public Result<Long> create(@Valid @RequestBody TeamCreateRequest request) {
        return Result.success(teamService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresPermissions("sys:team:edit")
    @Log(module = "团队管理", action = "编辑团队")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeamCreateRequest request) {
        teamService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:team:delete")
    @Log(module = "团队管理", action = "删除团队")
    public Result<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @RequiresPermissions("sys:team:assignRoles")
    @Log(module = "团队管理", action = "分配团队角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        teamService.assignRoles(id, request.getRoleIds());
        return Result.success();
    }

    @PutMapping("/{id}/users")
    @RequiresPermissions("sys:team:list")
    @Log(module = "团队管理", action = "分配团队成员")
    public Result<Void> assignUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        teamService.assignUsers(id, userIds);
        return Result.success();
    }
}
