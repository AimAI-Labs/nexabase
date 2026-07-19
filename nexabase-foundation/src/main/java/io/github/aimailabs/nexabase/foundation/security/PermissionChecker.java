package io.github.aimailabs.nexabase.foundation.security;

import java.util.Set;

/**
 * 权限校验 SPI 接口。
 * <p>
 * 由 {@link PermissionAspect} 调用，校验当前用户是否具备指定权限或角色。
 * 默认实现 {@code RedisPermissionChecker} 位于 {@code nexabase-auth-api} 模块，
 * 从 Redis 缓存读取用户权限集合；auth 模块自身提供 {@code LocalPermissionChecker}
 * 走本地 Service 查询以避免 Feign 自调用。
 * <p>
 * 业务服务引入 {@code nexabase-auth-api} 后自动注册默认实现；如需自定义，
 * 实现此接口并注册为 Bean 即可覆盖默认行为。
 */
public interface PermissionChecker {

    /**
     * 校验用户是否具备指定权限。
     *
     * @param userId     用户 ID
     * @param permission 权限标识（如 {@code sys:user:add}）
     * @return 具备返回 {@code true}
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 校验用户是否具备任一权限。
     *
     * @param userId      用户 ID
     * @param permissions 权限标识列表
     * @return 具备任一即返回 {@code true}
     */
    boolean hasAnyPermission(Long userId, String... permissions);

    /**
     * 校验用户是否具备全部权限。
     *
     * @param userId      用户 ID
     * @param permissions 权限标识列表
     * @return 全部具备返回 {@code true}
     */
    boolean hasAllPermissions(Long userId, String... permissions);

    /**
     * 校验用户是否具备指定角色。
     *
     * @param userId 用户 ID
     * @param role   角色标识（如 {@code SUPER_ADMIN}）
     * @return 具备返回 {@code true}
     */
    boolean hasRole(Long userId, String role);

    /**
     * 校验用户是否具备任一角色。
     *
     * @param userId 用户 ID
     * @param roles  角色标识列表
     * @return 具备任一即返回 {@code true}
     */
    boolean hasAnyRole(Long userId, String... roles);

    /**
     * 获取用户拥有的全部权限标识集合。
     * <p>
     * 供 {@code userinfo} 等接口复用，避免重复查询。
     *
     * @param userId 用户 ID
     * @return 权限标识集合，超管返回含 {@code *} 通配符
     */
    Set<String> getPermissions(Long userId);
}
