package io.github.aimailabs.nexabase.auth.api.security;

import io.github.aimailabs.nexabase.auth.api.ApiConstants;
import io.github.aimailabs.nexabase.auth.api.feign.AuthFeignClient;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.PermissionChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 基于 Redis 缓存的 {@link PermissionChecker} 默认实现。
 * <p>
 * 鉴权流程：
 * <ol>
 *   <li>从 Redis 读取 {@code auth:perms:{userId}} 与 {@code auth:roles:{userId}}</li>
 *   <li>缓存命中 → 直接校验（含 {@code *} 通配符则全放行，超管短路）</li>
 *   <li>缓存未命中 → 通过 {@link AuthFeignClient} 回源查询，回填缓存（TTL 7 天）</li>
 *   <li>Feign 失败 → 降级返回空集合（fail-closed）</li>
 * </ol>
 * 业务服务引入 {@code nexabase-auth-api} 后自动注册此实现。
 */
@Slf4j
public class RedisPermissionChecker implements PermissionChecker {

    private final StringRedisTemplate redisTemplate;
    private final AuthFeignClient authFeignClient;

    public RedisPermissionChecker(StringRedisTemplate redisTemplate, AuthFeignClient authFeignClient) {
        this.redisTemplate = redisTemplate;
        this.authFeignClient = authFeignClient;
    }

    @Override
    public boolean hasPermission(Long userId, String permission) {
        Set<String> perms = getPermissions(userId);
        return perms.contains(ApiConstants.SUPER_ADMIN_PERM) || perms.contains(permission);
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissions) {
        Set<String> perms = getPermissions(userId);
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
        Set<String> perms = getPermissions(userId);
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
        Set<String> roles = getRoles(userId);
        return roles.contains(ApiConstants.SUPER_ADMIN_ROLE) || roles.contains(role);
    }

    @Override
    public boolean hasAnyRole(Long userId, String... roles) {
        Set<String> userRoles = getRoles(userId);
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
        String key = ApiConstants.PERMS_CACHE_PREFIX + userId;
        Set<String> perms = readSet(key);
        if (perms == null) {
            // 缓存未命中，回源
            perms = loadPermissionsFromRemote(userId);
            writeSet(key, perms);
        }
        return perms;
    }

    /**
     * 获取用户角色集合（带缓存）。
     */
    public Set<String> getRoles(Long userId) {
        String key = ApiConstants.ROLES_CACHE_PREFIX + userId;
        Set<String> roles = readSet(key);
        if (roles == null) {
            roles = loadRolesFromRemote(userId);
            writeSet(key, roles);
        }
        return roles;
    }

    private Set<String> loadPermissionsFromRemote(Long userId) {
        try {
            Result<Set<String>> result = authFeignClient.getUserPermissionKeys(userId);
            if (result != null && result.getData() != null) {
                return new HashSet<>(result.getData());
            }
        } catch (Exception e) {
            log.warn("回源加载用户权限失败，userId={}: {}", userId, e.getMessage());
        }
        return Collections.emptySet();
    }

    private Set<String> loadRolesFromRemote(Long userId) {
        try {
            Result<Set<String>> result = authFeignClient.getUserRoleCodes(userId);
            if (result != null && result.getData() != null) {
                return new HashSet<>(result.getData());
            }
        } catch (Exception e) {
            log.warn("回源加载用户角色失败，userId={}: {}", userId, e.getMessage());
        }
        return Collections.emptySet();
    }

    private Set<String> readSet(String key) {
        Set<String> members = redisTemplate.opsForSet().members(key);
        // Redis 返回 null 表示 key 不存在；返回空集合表示 key 存在但无成员
        // 这里将 null 视为未命中，空集合视为已命中（避免缓存穿透时反复回源）
        return members;
    }

    private void writeSet(String key, Set<String> values) {
        if (values == null || values.isEmpty()) {
            // 空集合写入一个占位标记并设置较短 TTL，防止缓存穿透
            redisTemplate.opsForSet().add(key, "__empty__");
            redisTemplate.expire(key, Duration.ofMinutes(5));
            return;
        }
        String[] arr = values.toArray(new String[0]);
        redisTemplate.opsForSet().add(key, arr);
        redisTemplate.expire(key, Duration.ofSeconds(ApiConstants.CACHE_TTL_SECONDS));
    }
}
