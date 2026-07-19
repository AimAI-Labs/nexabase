package io.github.aimailabs.nexabase.auth.security;

import io.github.aimailabs.nexabase.auth.api.ApiConstants;
import io.github.aimailabs.nexabase.auth.service.PermissionCacheService;
import io.github.aimailabs.nexabase.foundation.security.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * auth 模块本地权限校验器。
 * <p>
 * 直接调用 {@link PermissionCacheService} 查询权限，避免 Feign 自调用
 * （auth 自身的 Controller 鉴权若走 {@code RedisPermissionChecker} 会触发 Feign 回源到自身）。
 * <p>
 * 标注 {@link Primary} 覆盖 auth-api 的 {@code RedisPermissionChecker}，
 * 使 auth 模块内 {@code PermissionAspect} 注入此实现。
 * <p>
 * 其他业务服务仍使用 {@code RedisPermissionChecker}（Redis + Feign 标准路径）。
 */
@Component
@Primary
@RequiredArgsConstructor
public class LocalPermissionChecker implements PermissionChecker {

    private final PermissionCacheService cacheService;

    @Override
    public boolean hasPermission(Long userId, String permission) {
        Set<String> perms = cacheService.getPermissions(userId);
        return perms.contains(ApiConstants.SUPER_ADMIN_PERM) || perms.contains(permission);
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissions) {
        Set<String> perms = cacheService.getPermissions(userId);
        if (perms.contains(ApiConstants.SUPER_ADMIN_PERM)) {
            return true;
        }
        for (String p : permissions) {
            if (perms.contains(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(Long userId, String... permissions) {
        Set<String> perms = cacheService.getPermissions(userId);
        if (perms.contains(ApiConstants.SUPER_ADMIN_PERM)) {
            return true;
        }
        for (String p : permissions) {
            if (!perms.contains(p)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasRole(Long userId, String role) {
        Set<String> roles = cacheService.getRoles(userId);
        return roles.contains(ApiConstants.SUPER_ADMIN_ROLE) || roles.contains(role);
    }

    @Override
    public boolean hasAnyRole(Long userId, String... roles) {
        Set<String> userRoles = cacheService.getRoles(userId);
        if (userRoles.contains(ApiConstants.SUPER_ADMIN_ROLE)) {
            return true;
        }
        for (String r : roles) {
            if (userRoles.contains(r)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getPermissions(Long userId) {
        return cacheService.getPermissions(userId);
    }
}
