package io.github.aimailabs.nexabase.auth.api.feign;

import io.github.aimailabs.nexabase.auth.api.dto.auth.UserInfoDTO;
import io.github.aimailabs.nexabase.foundation.common.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

/**
 * {@link AuthFeignClient} 降级实现。
 * <p>
 * 采用 fail-closed 策略：auth 服务不可用时返回空集合，鉴权走拒绝（安全优先）。
 * 仅输出 WARN 日志，不抛异常。
 * <p>
 * {@code RedisPermissionChecker} 在调 Feign 前先读 Redis 缓存，缓存命中则不触发 Feign，
 * 降级时缓存仍可用，避免因 auth 短暂不可用导致所有用户被锁。
 */
@Slf4j
public class AuthFeignClientFallback implements AuthFeignClient {

    @Override
    public Result<Set<String>> getUserPermissionKeys(Long userId) {
        log.warn("调用 auth 服务获取用户权限失败，降级返回空集合。userId={}", userId);
        return Result.success(Collections.emptySet());
    }

    @Override
    public Result<Set<String>> getUserRoleCodes(Long userId) {
        log.warn("调用 auth 服务获取用户角色失败，降级返回空集合。userId={}", userId);
        return Result.success(Collections.emptySet());
    }

    @Override
    public Result<UserInfoDTO> getUserInfo(Long userId) {
        log.warn("调用 auth 服务获取用户信息失败，降级返回 null。userId={}", userId);
        return Result.success(null);
    }
}
