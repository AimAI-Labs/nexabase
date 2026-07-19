package io.github.aimailabs.nexabase.auth.service;

import io.github.aimailabs.nexabase.auth.api.ApiConstants;
import io.github.aimailabs.nexabase.auth.entity.SysUser;
import io.github.aimailabs.nexabase.auth.mapper.SysRoleMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysTeamRoleMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserTeamMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserRoleMapper;
import io.github.aimailabs.nexabase.auth.entity.SysUserRole;
import io.github.aimailabs.nexabase.auth.entity.SysTeamRole;
import io.github.aimailabs.nexabase.auth.entity.SysUserTeam;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限缓存服务。
 * <p>
 * 统一管理权限/角色缓存的读取、回源与失效，以及 JWT 版本号与黑名单操作。
 * <p>
 * 缓存键：
 * <ul>
 *   <li>{@code auth:perms:{userId}} — 权限标识集合</li>
 *   <li>{@code auth:roles:{userId}} — 角色 code 集合</li>
 *   <li>{@code auth:jwt:version:{userId}} — JWT 版本号</li>
 *   <li>{@code auth:jwt:blacklist:{jti}} — Token 黑名单标记</li>
 * </ul>
 * 超管（SUPER_ADMIN）短路：不查权限 SQL，直接返回 {@code ["*"]}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private final StringRedisTemplate redis;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysTeamRoleMapper teamRoleMapper;
    private final SysUserTeamMapper userTeamMapper;

    /**
     * 获取用户权限标识集合（带缓存）。
     * <p>
     * 超管返回含 {@code *}，直接放行所有权限。
     */
    public Set<String> getPermissions(Long userId) {
        Set<String> roles = getRoles(userId);
        if (roles.contains(ApiConstants.SUPER_ADMIN_ROLE)) {
            return Set.of(ApiConstants.SUPER_ADMIN_PERM);
        }
        String key = ApiConstants.PERMS_CACHE_PREFIX + userId;
        Set<String> cached = readSet(key);
        if (cached != null) {
            return cached;
        }
        List<String> perms = userMapper.selectPermissionKeysByUserId(userId);
        Set<String> permSet = new HashSet<>(perms);
        writeSet(key, permSet);
        return permSet;
    }

    /**
     * 获取用户角色 code 集合（带缓存）。
     */
    public Set<String> getRoles(Long userId) {
        String key = ApiConstants.ROLES_CACHE_PREFIX + userId;
        Set<String> cached = readSet(key);
        if (cached != null) {
            return cached;
        }
        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        Set<String> roleSet = new HashSet<>(roles);
        writeSet(key, roleSet);
        return roleSet;
    }

    /**
     * 清除指定用户的权限与角色缓存。
     */
    public void evictUser(Long userId) {
        redis.delete(ApiConstants.PERMS_CACHE_PREFIX + userId);
        redis.delete(ApiConstants.ROLES_CACHE_PREFIX + userId);
    }

    /**
     * 批量清除用户缓存。
     */
    public void evictUsers(Collection<Long> userIds) {
        userIds.forEach(this::evictUser);
    }

    /**
     * 角色权限/用户角色变更时，清除所有持有该角色的用户缓存（含团队继承）。
     */
    public void evictByRoleId(Long roleId) {
        List<Long> userIds = new ArrayList<>(userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId).toList());
        List<Long> teamIds = teamRoleMapper.selectList(new LambdaQueryWrapper<SysTeamRole>().eq(SysTeamRole::getRoleId, roleId))
                .stream().map(SysTeamRole::getTeamId).toList();
        for (Long teamId : teamIds) {
            userIds.addAll(userTeamMapper.selectList(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getTeamId, teamId))
                    .stream().map(SysUserTeam::getUserId).toList());
        }
        evictUsers(userIds);
    }

    /**
     * 团队角色变更时，清除该团队下所有用户的缓存。
     */
    public void evictByTeamId(Long teamId) {
        List<Long> userIds = userTeamMapper.selectList(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getTeamId, teamId))
                .stream().map(SysUserTeam::getUserId).toList();
        evictUsers(userIds);
    }

    /**
     * 获取用户当前 JWT 版本号（带缓存）。
     */
    public Long getJwtVersion(Long userId) {
        String key = ApiConstants.JWT_VERSION_PREFIX + userId;
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        redis.opsForValue().set(key, String.valueOf(user.getJwtVersion()),
                Duration.ofSeconds(ApiConstants.CACHE_TTL_SECONDS));
        return user.getJwtVersion();
    }

    /**
     * 递增用户 JWT 版本号（踢下线），并清除该用户所有缓存。
     */
    public void incrementJwtVersion(Long userId) {
        userMapper.incrementJwtVersion(userId);
        redis.delete(ApiConstants.JWT_VERSION_PREFIX + userId);
        evictUser(userId);
    }

    /**
     * 将 Token 的 jti 加入黑名单。
     *
     * @param jti JWT ID
     * @param ttl 剩余有效期
     */
    public void blacklistToken(String jti, Duration ttl) {
        redis.opsForValue().set(ApiConstants.JWT_BLACKLIST_PREFIX + jti, "1", ttl);
    }

    /**
     * 判断 Token 是否在黑名单中。
     */
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(ApiConstants.JWT_BLACKLIST_PREFIX + jti));
    }

    /**
     * 读取 Redis Set 缓存。
     * <p>
     * 返回 null 表示未命中（key 不存在）；空集合表示已命中但无数据（占位标记）。
     * 占位标记 {@code __empty__} 会被过滤掉。
     */
    private Set<String> readSet(String key) {
        Set<String> members = redis.opsForSet().members(key);
        if (members == null) {
            return null;
        }
        members.remove("__empty__");
        return members;
    }

    /**
     * 写入 Redis Set 缓存。
     * <p>
     * 空集合写入占位标记并设置较短 TTL（5 分钟），防止缓存穿透。
     */
    private void writeSet(String key, Set<String> values) {
        if (values.isEmpty()) {
            redis.opsForSet().add(key, "__empty__");
            redis.expire(key, Duration.ofMinutes(5));
            return;
        }
        redis.opsForSet().add(key, values.toArray(new String[0]));
        redis.expire(key, Duration.ofSeconds(ApiConstants.CACHE_TTL_SECONDS));
    }
}
