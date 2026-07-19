package io.github.aimailabs.nexabase.auth.api;

/**
 * auth-api 模块公共常量。
 * <p>
 * 统一管理 Redis 缓存键前缀、HTTP 头名称、内部接口路径等标识，
 * 确保各模块使用一致的约定。
 */
public final class ApiConstants {

    private ApiConstants() {
    }

    /** 权限缓存键前缀：{@code auth:perms:{userId}} → Set&lt;String&gt; 权限标识集合 */
    public static final String PERMS_CACHE_PREFIX = "auth:perms:";

    /** 角色缓存键前缀：{@code auth:roles:{userId}} → Set&lt;String&gt; 角色 code 集合 */
    public static final String ROLES_CACHE_PREFIX = "auth:roles:";

    /** JWT 版本号缓存键前缀：{@code auth:jwt:version:{userId}} → Long */
    public static final String JWT_VERSION_PREFIX = "auth:jwt:version:";

    /** JWT 黑名单缓存键前缀：{@code auth:jwt:blacklist:{jti}} → 1 */
    public static final String JWT_BLACKLIST_PREFIX = "auth:jwt:blacklist:";

    /** 超管角色 code，持有此角色的用户拥有全部权限（短路放行） */
    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";

    /** 超管通配权限标识，鉴权时全放行 */
    public static final String SUPER_ADMIN_PERM = "*";

    /** 内部接口路径前缀（仅供 Feign 调用，网关路由排除） */
    public static final String INTERNAL_PATH_PREFIX = "/api/v1/auth/internal";

    /** 权限/角色缓存默认 TTL（7 天，秒） */
    public static final long CACHE_TTL_SECONDS = 7 * 24 * 60 * 60L;
}
