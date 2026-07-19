package io.github.aimailabs.nexabase.foundation.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解。
 * <p>
 * 标注于 Controller 方法或类上，由 {@link PermissionAspect} 拦截校验当前用户是否具备指定的角色。
 * 角色标识对应 {@code sys_role.code}（如 {@code SUPER_ADMIN}）。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @RequiresRoles("SUPER_ADMIN")
 *
 * @RequiresRoles(value = {"ADMIN", "MANAGER"}, logical = Logical.OR)
 * }</pre>
 * <p>
 * 方法级注解优先于类级注解。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRoles {

    /**
     * 角色标识列表（对应 {@code sys_role.code}）。
     *
     * @return 角色标识数组
     */
    String[] value();

    /**
     * 多角色间的逻辑关系，默认 {@link Logical#AND}。
     *
     * @return 逻辑组合方式
     */
    Logical logical() default Logical.AND;
}
