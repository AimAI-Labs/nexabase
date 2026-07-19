package io.github.aimailabs.nexabase.auth.service;

import io.github.aimailabs.nexabase.auth.api.dto.auth.LoginRequest;
import io.github.aimailabs.nexabase.auth.api.dto.auth.TokenResponse;
import io.github.aimailabs.nexabase.auth.api.dto.auth.UserInfoDTO;
import io.github.aimailabs.nexabase.auth.entity.SysUser;
import io.github.aimailabs.nexabase.auth.mapper.SysUserMapper;
import io.github.aimailabs.nexabase.auth.security.JwtTokenService;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import io.github.aimailabs.nexabase.foundation.security.UserContext;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 认证服务。
 * <p>
 * 提供 JWT 双 Token 的登录、刷新、注销与当前用户信息查询。
 * <p>
 * 登录流程：用户名密码校验 → 状态校验 → 签发双 Token → 预热权限缓存。
 * 刷新流程：解析校验 → 类型校验 → jwt_version 校验 → 黑名单校验 → 旋转（旧 token 入黑名单）→ 签发新 Token。
 * 注销流程：解析 access token → jti 入黑名单（TTL = 剩余有效期）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService cacheService;

    /**
     * 登录。
     */
    public TokenResponse login(LoginRequest request) {
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已禁用");
        }
        String accessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getUsername(), user.getJwtVersion());
        String refreshToken = jwtTokenService.generateRefreshToken(
                user.getId(), user.getJwtVersion());
        // 预热权限缓存
        cacheService.getRoles(user.getId());
        cacheService.getPermissions(user.getId());
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenService.getAccessTokenExpiresInSeconds())
                .build();
    }

    /**
     * 刷新 Access Token。
     * <p>
     * Refresh Token 旋转：旧 refresh token 的 jti 加入黑名单，签发新的双 Token。
     */
    public TokenResponse refreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = jwtTokenService.parseAndVerify(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED, "刷新令牌无效或已过期");
        }
        if (!JwtTokenService.TYPE_REFRESH.equals(jwtTokenService.getTokenType(claims))) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "令牌类型错误");
        }
        Long userId = jwtTokenService.getUserId(claims);
        Long tokenVersion = jwtTokenService.getJwtVersion(claims);

        // 校验 jwt_version（账号是否被踢下线）
        Long currentVersion = cacheService.getJwtVersion(userId);
        if (currentVersion == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        if (tokenVersion == null || !tokenVersion.equals(currentVersion)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "账号已失效，请重新登录");
        }

        // 黑名单校验（防重放）
        String jti = jwtTokenService.getJti(claims);
        if (cacheService.isBlacklisted(jti)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID, "刷新令牌已失效");
        }

        // 旋转：旧 refresh token 入黑名单
        Date expiry = claims.getExpiration();
        long remainingMs = expiry.getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            cacheService.blacklistToken(jti, Duration.ofMillis(remainingMs));
        }

        // 查用户状态
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已禁用");
        }

        String newAccessToken = jwtTokenService.generateAccessToken(
                user.getId(), user.getUsername(), user.getJwtVersion());
        String newRefreshToken = jwtTokenService.generateRefreshToken(
                user.getId(), user.getJwtVersion());
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenService.getAccessTokenExpiresInSeconds())
                .build();
    }

    /**
     * 注销，将 access token 加入黑名单。
     */
    public void logout(String accessToken) {
        try {
            Claims claims = jwtTokenService.parseAndVerify(accessToken);
            String jti = jwtTokenService.getJti(claims);
            Date expiry = claims.getExpiration();
            long remainingMs = expiry.getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                cacheService.blacklistToken(jti, Duration.ofMillis(remainingMs));
            }
        } catch (Exception e) {
            // token 已无效，无需加入黑名单
            log.debug("注销时 token 解析失败（可能已过期）: {}", e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息。
     */
    public UserInfoDTO getCurrentUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "未认证");
        }
        return getUserInfo(userId);
    }

    /**
     * 获取指定用户完整信息（含角色/权限/团队），供内部 Feign 接口调用。
     */
    public UserInfoDTO getUserInfo(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        Set<String> roles = cacheService.getRoles(userId);
        Set<String> perms = cacheService.getPermissions(userId);
        List<Long> teamIds = userMapper.selectTeamIdsByUserId(userId);
        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(new ArrayList<>(roles))
                .permissions(new ArrayList<>(perms))
                .teamIds(teamIds)
                .build();
    }

    /**
     * 获取用户权限标识集合（供内部 Feign 接口调用）。
     */
    public Set<String> getUserPermissionKeys(Long userId) {
        return cacheService.getPermissions(userId);
    }

    /**
     * 获取用户角色 code 集合（供内部 Feign 接口调用）。
     */
    public Set<String> getUserRoleCodes(Long userId) {
        return cacheService.getRoles(userId);
    }
}
