package io.github.aimailabs.nexabase.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserCreateRequest;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserDTO;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserPageQuery;
import io.github.aimailabs.nexabase.auth.api.dto.user.UserUpdateRequest;
import io.github.aimailabs.nexabase.auth.entity.SysUser;
import io.github.aimailabs.nexabase.auth.entity.SysUserRole;
import io.github.aimailabs.nexabase.auth.entity.SysUserTeam;
import io.github.aimailabs.nexabase.auth.mapper.SysUserMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserRoleMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserTeamMapper;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理服务。
 * <p>
 * 提供用户 CRUD、重置密码、启用/禁用、分配角色等能力。
 * 禁用账号或重置密码时触发 JWT 版本递增（踢下线）。
 */
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserTeamMapper userTeamMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionCacheService cacheService;

    @Transactional
    public Long create(UserCreateRequest req) {
        if (userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername())) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setStatus(1);
        user.setJwtVersion(1L);
        userMapper.insert(user);

        if (req.getRoleIds() != null && !req.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), req.getRoleIds());
        }
        if (req.getTeamIds() != null && !req.getTeamIds().isEmpty()) {
            List<SysUserTeam> teams = req.getTeamIds().stream()
                    .map(tid -> new SysUserTeam(user.getId(), tid, 0))
                    .toList();
            userTeamMapper.insertBatch(teams);
        }
        return user.getId();
    }

    public void update(Long id, UserUpdateRequest req) {
        SysUser user = getOrThrow(id);
        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        userMapper.updateById(user);
    }

    @Transactional
    public void delete(Long id) {
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userTeamMapper.delete(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getUserId, id));
        cacheService.evictUser(id);
    }

    public UserDTO getById(Long id) {
        SysUser user = getOrThrow(id);
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id))
                .stream().map(SysUserRole::getRoleId).toList();
        List<Long> teamIds = userTeamMapper.selectList(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getUserId, id))
                .stream().map(SysUserTeam::getTeamId).toList();
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .roleIds(roleIds)
                .teamIds(teamIds)
                .build();
    }

    public Page<UserDTO> page(UserPageQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isBlank()) {
            wrapper.like(SysUser::getUsername, query.getUsername());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        Page<SysUser> page = userMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        Page<UserDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(u -> UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nickname(u.getNickname())
                .email(u.getEmail())
                .phone(u.getPhone())
                .status(u.getStatus())
                .createdAt(u.getCreatedAt())
                .build()).toList());
        return result;
    }

    /**
     * 重置密码为默认值 123456，并踢下线。
     */
    public void resetPassword(Long id) {
        SysUser user = getOrThrow(id);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
        cacheService.incrementJwtVersion(id);
    }

    /**
     * 切换账号状态。禁用时触发踢下线。
     */
    public void toggleStatus(Long id, Integer status) {
        SysUser user = getOrThrow(id);
        user.setStatus(status);
        userMapper.updateById(user);
        if (status == 0) {
            cacheService.incrementJwtVersion(id);
        }
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        getOrThrow(userId);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> list = roleIds.stream()
                    .map(rid -> new SysUserRole(userId, rid))
                    .toList();
            userRoleMapper.insertBatch(list);
        }
        cacheService.evictUser(userId);
    }

    private SysUser getOrThrow(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        return user;
    }
}
