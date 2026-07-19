package io.github.aimailabs.nexabase.auth.api.feign;

import io.github.aimailabs.nexabase.auth.api.dto.auth.UserInfoDTO;
import io.github.aimailabs.nexabase.foundation.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

/**
 * auth 服务对外 Feign 客户端。
 * <p>
 * 供其他微服务调用 auth 的内部接口，查询用户权限/角色/信息。
 * 调用路径为 {@code /api/v1/auth/internal/**}，网关路由排除该前缀，仅供服务间调用。
 * <p>
 * 降级策略 {@link AuthFeignClientFallback}：fail-closed，返回空集合（鉴权走拒绝）。
 */
@FeignClient(name = "nexabase-auth", fallback = AuthFeignClientFallback.class)
public interface AuthFeignClient {

    /**
     * 查询用户拥有的全部权限标识集合。
     *
     * @param userId 用户 ID
     * @return 权限标识集合，超管返回含 {@code *}
     */
    @GetMapping("/api/v1/auth/internal/permissions/{userId}")
    Result<Set<String>> getUserPermissionKeys(@PathVariable("userId") Long userId);

    /**
     * 查询用户拥有的全部角色 code 集合。
     *
     * @param userId 用户 ID
     * @return 角色 code 集合
     */
    @GetMapping("/api/v1/auth/internal/roles/{userId}")
    Result<Set<String>> getUserRoleCodes(@PathVariable("userId") Long userId);

    /**
     * 查询用户完整信息（含角色/权限/团队）。
     *
     * @param userId 用户 ID
     * @return 用户信息
     */
    @GetMapping("/api/v1/auth/internal/userinfo/{userId}")
    Result<UserInfoDTO> getUserInfo(@PathVariable("userId") Long userId);
}
