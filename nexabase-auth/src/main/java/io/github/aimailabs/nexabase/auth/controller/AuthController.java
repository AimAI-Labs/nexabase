package io.github.aimailabs.nexabase.auth.controller;

import io.github.aimailabs.nexabase.auth.api.dto.auth.LoginRequest;
import io.github.aimailabs.nexabase.auth.api.dto.auth.RefreshTokenRequest;
import io.github.aimailabs.nexabase.auth.api.dto.auth.TokenResponse;
import io.github.aimailabs.nexabase.auth.api.dto.auth.UserInfoDTO;
import io.github.aimailabs.nexabase.auth.service.AuthService;
import io.github.aimailabs.nexabase.foundation.common.Result;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 认证服务 / 认证接口
 * <p>
 * 提供登录、刷新、注销、用户信息查询等能力，并包含供网关/Feign 调用的内部接口。
 * 登录与刷新接口无需鉴权（网关白名单放行）。
 *
 * @module nexabase-auth
 */
@Tag(name = "认证接口", description = "提供登录、刷新、注销、用户信息查询等功能")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录，校验账号密码并签发双 Token（Access + Refresh）。
     *
     * @param request 登录请求体，包含用户名与密码
     * @return 登录成功后的双 Token 信息
     */
    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 使用 Refresh Token 换取新的 Access Token（Refresh Token 未过期且未登出时有效）。
     *
     * @param request 刷新请求体，包含长期有效的 refreshToken
     * @return 刷新后的双 Token 信息
     */
    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }

    /**
     * 用户注销，将当前 Access Token 加入黑名单使其立即失效。
     *
     * @param authorization 请求头 Authorization（Bearer Token），可空
     * @return 成功响应
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7));
        }
        return Result.success();
    }

    /**
     * 获取当前登录用户的基本信息、角色、权限及所属团队。
     *
     * @return 当前登录用户的详细信息
     */
    @Operation(summary = "获取当前登录用户信息", description = "获取当前用户的基本信息、角色、权限及所属团队")
    @ApiResponse(responseCode = "200", description = "成功")
    @ApiResponse(responseCode = "401", description = "未认证或认证已过期")
    @GetMapping("/userinfo")
    public Result<UserInfoDTO> userinfo() {
        return Result.success(authService.getCurrentUserInfo());
    }

    /**
     * 获取当前登录用户的权限标识列表，供前端按钮级权限指令使用。
     *
     * @return 当前用户的权限标识（code）列表
     */
    @GetMapping("/permissions")
    public Result<List<String>> permissions() {
        Long userId = UserContext.getCurrentUserId();
        Set<String> perms = authService.getUserPermissionKeys(userId);
        return Result.success(new ArrayList<>(perms));
    }

    // ==================== 内部接口（仅供 Feign 调用，网关路由排除） ====================

    /**
     * 根据 userId 查询其权限标识列表（内部接口，仅供 Feign 调用）。
     *
     * @param userId 用户 ID
     * @return 该用户的权限标识（code）列表
     */
    @GetMapping("/internal/permissions/{userId}")
    public Result<Set<String>> internalGetPermissions(@PathVariable Long userId) {
        return Result.success(authService.getUserPermissionKeys(userId));
    }

    /**
     * 根据 userId 查询其角色编码列表（内部接口，仅供 Feign 调用）。
     *
     * @param userId 用户 ID
     * @return 该用户的角色编码列表
     */
    @GetMapping("/internal/roles/{userId}")
    public Result<Set<String>> internalGetRoles(@PathVariable Long userId) {
        return Result.success(authService.getUserRoleCodes(userId));
    }

    /**
     * 根据 userId 查询用户详情（内部接口，仅供 Feign 调用）。
     *
     * @param userId 用户 ID
     * @return 用户基本信息
     */
    @GetMapping("/internal/userinfo/{userId}")
    public Result<UserInfoDTO> internalGetUserInfo(@PathVariable Long userId) {
        return Result.success(authService.getUserInfo(userId));
    }
}
