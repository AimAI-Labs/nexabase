package io.github.aimailabs.nexabase.foundation.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解。
 * <p>
 * 标注于 Controller 方法或类上，由 {@link PermissionAspect} 拦截校验当前用户是否具备指定的权限标识。
 * 权限标识对应 {@code sys_permission.permission_key}（如 {@code sys:user:add}）。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 单个权限
 * @RequiresPermissions("sys:user:add")
 *
 * // 多个权限，需全部满足
 * @RequiresPermissions(value = {"sys:user:add", "sys:user:edit"}, logical = Logical.AND)
 *
 * // 任一权限即可
 * @RequiresPermissions(value = {"sys:user:add", "sys:user:edit"}, logical = Logical.OR)
 * }</pre>
 * <p>
 * 方法级注解优先于类级注解。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermissions {

    /**
     * 权限标识列表（对应 {@code sys_permission.permission_key}）。
     *
     * @return 权限标识数组
     */
    String[] value();

    /**
     * 多权限间的逻辑关系，默认 {@link Logical#AND}。
     *
     * @return 逻辑组合方式
     */
    Logical logical() default Logical.AND;
}
