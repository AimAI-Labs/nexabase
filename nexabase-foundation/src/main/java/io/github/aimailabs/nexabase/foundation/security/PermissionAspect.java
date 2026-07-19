package io.github.aimailabs.nexabase.foundation.security;

import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 权限校验切面。
 * <p>
 * 拦截 {@link RequiresPermissions} 与 {@link RequiresRoles} 注解，通过
 * {@link PermissionChecker}（可选注入）校验当前用户是否具备所需权限或角色。
 * <p>
 * 校验规则：
 * <ul>
 *   <li>未登录（{@link UserContext#getCurrentUserId()} 为 null）→ 抛出 {@code UNAUTHORIZED}</li>
 *   <li>未注入 {@link PermissionChecker} → 放行并输出 WARN（兼容未接入 auth 的服务）</li>
 *   <li>校验失败 → 抛出 {@code FORBIDDEN}</li>
 *   <li>超管判断下沉至 {@link PermissionChecker} 实现（权限集合含 {@code *} 即放行）</li>
 * </ul>
 * 方法级注解优先于类级注解。
 */
@Slf4j
@Aspect
public class PermissionAspect {

    private final ObjectProvider<PermissionChecker> permissionCheckerProvider;

    public PermissionAspect(ObjectProvider<PermissionChecker> permissionCheckerProvider) {
        this.permissionCheckerProvider = permissionCheckerProvider;
    }

    /**
     * 拦截 {@link RequiresPermissions} 注解（方法级或类级）。
     */
    @Around("@within(io.github.aimailabs.nexabase.foundation.security.RequiresPermissions) || "
            + "@annotation(io.github.aimailabs.nexabase.foundation.security.RequiresPermissions)")
    public Object aroundRequiresPermissions(ProceedingJoinPoint joinPoint) throws Throwable {
        RequiresPermissions annotation = resolvePermissionAnnotation(joinPoint);
        if (annotation != null) {
            checkPermissions(annotation);
        }
        return joinPoint.proceed();
    }

    /**
     * 拦截 {@link RequiresRoles} 注解（方法级或类级）。
     */
    @Around("@within(io.github.aimailabs.nexabase.foundation.security.RequiresRoles) || "
            + "@annotation(io.github.aimailabs.nexabase.foundation.security.RequiresRoles)")
    public Object aroundRequiresRoles(ProceedingJoinPoint joinPoint) throws Throwable {
        RequiresRoles annotation = resolveRoleAnnotation(joinPoint);
        if (annotation != null) {
            checkRoles(annotation);
        }
        return joinPoint.proceed();
    }

    private RequiresPermissions resolvePermissionAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequiresPermissions.class);
        }
        return annotation;
    }

    private RequiresRoles resolveRoleAnnotation(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequiresRoles annotation = method.getAnnotation(RequiresRoles.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(RequiresRoles.class);
        }
        return annotation;
    }

    private void checkPermissions(RequiresPermissions annotation) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未认证或认证已过期");
        }
        PermissionChecker checker = permissionCheckerProvider.getIfAvailable();
        if (checker == null) {
            log.warn("未注入 PermissionChecker，权限校验被跳过。请引入 nexabase-auth-api 以启用鉴权。");
            return;
        }
        String[] permissions = annotation.value();
        boolean pass;
        if (annotation.logical() == Logical.AND) {
            pass = checker.hasAllPermissions(userId, permissions);
        } else {
            pass = checker.hasAnyPermission(userId, permissions);
        }
        if (!pass) {
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "无访问权限，缺少权限: " + Arrays.toString(permissions));
        }
    }

    private void checkRoles(RequiresRoles annotation) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未认证或认证已过期");
        }
        PermissionChecker checker = permissionCheckerProvider.getIfAvailable();
        if (checker == null) {
            log.warn("未注入 PermissionChecker，角色校验被跳过。请引入 nexabase-auth-api 以启用鉴权。");
            return;
        }
        String[] roles = annotation.value();
        boolean pass;
        if (annotation.logical() == Logical.AND) {
            pass = hasAllRoles(checker, userId, roles);
        } else {
            pass = checker.hasAnyRole(userId, roles);
        }
        if (!pass) {
            throw new BusinessException(ResultCode.FORBIDDEN,
                    "无访问权限，缺少角色: " + Arrays.toString(roles));
        }
    }

    private boolean hasAllRoles(PermissionChecker checker, Long userId, String[] roles) {
        for (String role : roles) {
            if (!checker.hasRole(userId, role)) {
                return false;
            }
        }
        return true;
    }
}
