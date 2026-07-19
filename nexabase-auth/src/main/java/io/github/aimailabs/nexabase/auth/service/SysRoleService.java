package io.github.aimailabs.nexabase.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.aimailabs.nexabase.auth.api.ApiConstants;
import io.github.aimailabs.nexabase.auth.api.dto.role.RoleCreateRequest;
import io.github.aimailabs.nexabase.auth.api.dto.role.RoleDTO;
import io.github.aimailabs.nexabase.auth.api.dto.role.RoleUpdateRequest;
import io.github.aimailabs.nexabase.auth.entity.SysRole;
import io.github.aimailabs.nexabase.auth.entity.SysRolePermission;
import io.github.aimailabs.nexabase.auth.entity.SysUserRole;
import io.github.aimailabs.nexabase.auth.mapper.SysRoleMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysRolePermissionMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserRoleMapper;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务。
 * <p>
 * 提供角色 CRUD 与权限分配。权限变更后清除所有持有该角色的用户缓存。
 * 超管角色（SUPER_ADMIN）不可删除。
 */
@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionCacheService cacheService;

    public Long create(RoleCreateRequest req) {
        if (roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, req.getCode())) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "角色标识已存在");
        }
        SysRole role = new SysRole();
        role.setName(req.getName());
        role.setCode(req.getCode());
        role.setDescription(req.getDescription());
        role.setStatus(1);
        roleMapper.insert(role);
        return role.getId();
    }

    public void update(Long id, RoleUpdateRequest req) {
        SysRole role = getOrThrow(id);
        if (req.getName() != null) role.setName(req.getName());
        if (req.getDescription() != null) role.setDescription(req.getDescription());
        if (req.getStatus() != null) role.setStatus(req.getStatus());
        roleMapper.updateById(role);
    }

    @Transactional
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            return;
        }
        if (ApiConstants.SUPER_ADMIN_ROLE.equals(role.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "超级管理员角色不可删除");
        }
        roleMapper.deleteById(id);
        rolePermissionMapper.deleteByRoleId(id);
        cacheService.evictByRoleId(id);
    }

    public List<RoleDTO> list() {
        return roleMapper.selectList(null).stream().map(this::toDTO).toList();
    }

    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getOrThrow(roleId);
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> list = permissionIds.stream()
                    .map(pid -> new SysRolePermission(roleId, pid))
                    .toList();
            rolePermissionMapper.insertBatch(list);
        }
        cacheService.evictByRoleId(roleId);
    }

    /**
     * 批量为用户赋予该角色（不影响用户已有的其他角色）。
     */
    @Transactional
    public void assignToUsers(Long roleId, List<Long> userIds) {
        getOrThrow(roleId);
        for (Long userId : userIds) {
            Long count = userRoleMapper.selectCount(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, userId)
                            .eq(SysUserRole::getRoleId, roleId));
            if (count == 0) {
                userRoleMapper.insert(new SysUserRole(userId, roleId));
            }
        }
        cacheService.evictUsers(userIds);
    }

    public List<Long> getPermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    private SysRole getOrThrow(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        return role;
    }

    private RoleDTO toDTO(SysRole role) {
        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .code(role.getCode())
                .description(role.getDescription())
                .status(role.getStatus())
                .createdAt(role.getCreatedAt())
                .build();
    }
}
