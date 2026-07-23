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
 * 系统管理 / 团队管理接口
 * <p>
 * 提供团队的树形查询、增删改及角色/成员分配。
 *
 * @module nexabase-auth
 */
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class SysTeamController {

    private final SysTeamService teamService;

    /**
     * 查询团队树形结构（支持无限层级嵌套）。
     *
     * @return 团队树
     */
    @GetMapping("/tree")
    @RequiresPermissions("sys:team:list")
    public Result<List<TeamDTO>> tree() {
        return Result.success(teamService.getTree());
    }

    /**
     * 新增团队，可指定父团队以挂载到树结构。
     *
     * @param request 新增团队请求体（名称、描述、父团队 ID）
     * @return 创建成功的团队 ID
     */
    @PostMapping
    @RequiresPermissions("sys:team:add")
    @Log(module = "团队管理", action = "新增团队")
    public Result<Long> create(@Valid @RequestBody TeamCreateRequest request) {
        return Result.success(teamService.create(request));
    }

    /**
     * 更新团队基本信息。
     *
     * @param id      团队 ID
     * @param request 更新请求体
     * @return 成功响应
     */
    @PutMapping("/{id}")
    @RequiresPermissions("sys:team:edit")
    @Log(module = "团队管理", action = "编辑团队")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TeamCreateRequest request) {
        teamService.update(id, request);
        return Result.success();
    }

    /**
     * 删除团队（逻辑删除）。
     *
     * @param id 团队 ID
     * @return 成功响应
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions("sys:team:delete")
    @Log(module = "团队管理", action = "删除团队")
    public Result<Void> delete(@PathVariable Long id) {
        teamService.delete(id);
        return Result.success();
    }

    /**
     * 为团队分配角色（全量覆盖式分配）。
     *
     * @param id      团队 ID
     * @param request 角色分配请求体，包含角色 ID 列表
     * @return 成功响应
     */
    @PutMapping("/{id}/roles")
    @RequiresPermissions("sys:team:assignRoles")
    @Log(module = "团队管理", action = "分配团队角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        teamService.assignRoles(id, request.getRoleIds());
        return Result.success();
    }

    /**
     * 为团队批量分配成员（全量覆盖式分配）。
     *
     * @param id      团队 ID
     * @param userIds 成员用户 ID 列表
     * @return 成功响应
     */
    @PutMapping("/{id}/users")
    @RequiresPermissions("sys:team:list")
    @Log(module = "团队管理", action = "分配团队成员")
    public Result<Void> assignUsers(@PathVariable Long id, @RequestBody List<Long> userIds) {
        teamService.assignUsers(id, userIds);
        return Result.success();
    }
}
