package io.github.aimailabs.nexabase.auth.service;

import io.github.aimailabs.nexabase.auth.api.dto.permission.PermissionDTO;
import io.github.aimailabs.nexabase.auth.entity.SysPermission;
import io.github.aimailabs.nexabase.auth.mapper.SysPermissionMapper;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限资源服务。
 * <p>
 * 提供权限树的查询与 CRUD。权限树通过内存组装父子结构返回。
 */
@Service
@RequiredArgsConstructor
public class SysPermissionService {

    private final SysPermissionMapper permissionMapper;

    /**
     * 查询权限树（全量加载后内存组装父子结构）。
     */
    public List<PermissionDTO> getTree() {
        List<SysPermission> all = permissionMapper.selectAllOrdered();
        Map<Long, PermissionDTO> dtoMap = new LinkedHashMap<>();
        for (SysPermission p : all) {
            dtoMap.put(p.getId(), toDTO(p));
        }
        List<PermissionDTO> roots = new ArrayList<>();
        for (SysPermission p : all) {
            PermissionDTO dto = dtoMap.get(p.getId());
            Long parentId = p.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(dto);
            } else {
                PermissionDTO parent = dtoMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    public Long create(SysPermission permission) {
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        if (permission.getSortOrder() == null) {
            permission.setSortOrder(0);
        }
        permissionMapper.insert(permission);
        return permission.getId();
    }

    public void update(Long id, SysPermission permission) {
        SysPermission existing = getOrThrow(id);
        if (permission.getName() != null) existing.setName(permission.getName());
        if (permission.getType() != null) existing.setType(permission.getType());
        if (permission.getPermissionKey() != null) existing.setPermissionKey(permission.getPermissionKey());
        if (permission.getPath() != null) existing.setPath(permission.getPath());
        if (permission.getMethod() != null) existing.setMethod(permission.getMethod());
        if (permission.getComponent() != null) existing.setComponent(permission.getComponent());
        if (permission.getIcon() != null) existing.setIcon(permission.getIcon());
        if (permission.getSortOrder() != null) existing.setSortOrder(permission.getSortOrder());
        permissionMapper.updateById(existing);
    }

    public void delete(Long id) {
        // 检查是否有子权限
        List<SysPermission> children = permissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, id));
        if (!children.isEmpty()) {
            throw new BusinessException(ResultCode.CONFLICT, "存在子权限，请先删除子权限");
        }
        permissionMapper.deleteById(id);
    }

    private SysPermission getOrThrow(Long id) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "权限不存在");
        }
        return permission;
    }

    private PermissionDTO toDTO(SysPermission p) {
        return PermissionDTO.builder()
                .id(p.getId())
                .parentId(p.getParentId())
                .name(p.getName())
                .type(p.getType())
                .permissionKey(p.getPermissionKey())
                .path(p.getPath())
                .method(p.getMethod())
                .component(p.getComponent())
                .icon(p.getIcon())
                .sortOrder(p.getSortOrder())
                .build();
    }
}
