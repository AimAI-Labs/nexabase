package io.github.aimailabs.nexabase.auth.service;

import io.github.aimailabs.nexabase.auth.api.dto.team.TeamCreateRequest;
import io.github.aimailabs.nexabase.auth.api.dto.team.TeamDTO;
import io.github.aimailabs.nexabase.auth.entity.SysTeam;
import io.github.aimailabs.nexabase.auth.entity.SysTeamRole;
import io.github.aimailabs.nexabase.auth.entity.SysUserTeam;
import io.github.aimailabs.nexabase.auth.mapper.SysTeamMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysTeamRoleMapper;
import io.github.aimailabs.nexabase.auth.mapper.SysUserTeamMapper;
import io.github.aimailabs.nexabase.foundation.common.ResultCode;
import io.github.aimailabs.nexabase.foundation.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 团队管理服务。
 * <p>
 * 提供团队树查询与 CRUD，以及团队角色继承分配。
 * 团队角色变更后清除该团队下所有用户的权限缓存。
 */
@Service
@RequiredArgsConstructor
public class SysTeamService {

    private final SysTeamMapper teamMapper;
    private final SysTeamRoleMapper teamRoleMapper;
    private final SysUserTeamMapper userTeamMapper;
    private final PermissionCacheService cacheService;

    /**
     * 查询团队树（全量加载后内存组装父子结构）。
     */
    public List<TeamDTO> getTree() {
        List<SysTeam> all = teamMapper.selectList(null);
        Map<Long, TeamDTO> dtoMap = new LinkedHashMap<>();
        for (SysTeam t : all) {
            TeamDTO dto = toDTO(t);
            dto.setRoleIds(teamRoleMapper.selectList(
                    new LambdaQueryWrapper<SysTeamRole>()
                            .eq(SysTeamRole::getTeamId, t.getId()))
                    .stream().map(SysTeamRole::getRoleId).toList());
            dtoMap.put(t.getId(), dto);
        }
        List<TeamDTO> roots = new ArrayList<>();
        for (SysTeam t : all) {
            TeamDTO dto = dtoMap.get(t.getId());
            Long parentId = t.getParentId();
            if (parentId == null || parentId == 0L) {
                roots.add(dto);
            } else {
                TeamDTO parent = dtoMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    public Long create(TeamCreateRequest req) {
        SysTeam team = new SysTeam();
        team.setParentId(req.getParentId());
        team.setName(req.getName());
        team.setType(req.getType());
        team.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        teamMapper.insert(team);
        return team.getId();
    }

    public void update(Long id, TeamCreateRequest req) {
        SysTeam team = getOrThrow(id);
        if (req.getParentId() != null) team.setParentId(req.getParentId());
        if (req.getName() != null) team.setName(req.getName());
        if (req.getType() != null) team.setType(req.getType());
        if (req.getSortOrder() != null) team.setSortOrder(req.getSortOrder());
        teamMapper.updateById(team);
    }

    @Transactional
    public void delete(Long id) {
        // 检查子团队
        if (!teamMapper.selectList(new LambdaQueryWrapper<SysTeam>().eq(SysTeam::getParentId, id).orderByAsc(SysTeam::getSortOrder)).isEmpty()) {
            throw new BusinessException(ResultCode.CONFLICT, "存在子团队，请先删除子团队");
        }
        teamMapper.deleteById(id);
        teamRoleMapper.delete(new LambdaQueryWrapper<SysTeamRole>().eq(SysTeamRole::getTeamId, id));
        userTeamMapper.delete(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getTeamId, id));
        cacheService.evictByTeamId(id);
    }

    /**
     * 为团队分配继承角色（全量覆盖）。
     */
    @Transactional
    public void assignRoles(Long teamId, List<Long> roleIds) {
        getOrThrow(teamId);
        teamRoleMapper.delete(new LambdaQueryWrapper<SysTeamRole>().eq(SysTeamRole::getTeamId, teamId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysTeamRole> list = roleIds.stream()
                    .map(rid -> new SysTeamRole(teamId, rid))
                    .toList();
            teamRoleMapper.insertBatch(list);
        }
        cacheService.evictByTeamId(teamId);
    }

    /**
     * 为团队分配成员（全量覆盖）。
     */
    @Transactional
    public void assignUsers(Long teamId, List<Long> userIds) {
        getOrThrow(teamId);
        // 清除旧成员缓存
        List<Long> oldUserIds = userTeamMapper.selectList(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getTeamId, teamId))
                .stream().map(SysUserTeam::getUserId).toList();
        cacheService.evictUsers(oldUserIds);

        userTeamMapper.delete(new LambdaQueryWrapper<SysUserTeam>().eq(SysUserTeam::getTeamId, teamId));
        if (userIds != null && !userIds.isEmpty()) {
            List<SysUserTeam> list = userIds.stream()
                    .map(uid -> new SysUserTeam(uid, teamId, 0))
                    .toList();
            userTeamMapper.insertBatch(list);
        }
        // 清除新成员缓存
        cacheService.evictUsers(userIds);
    }

    private SysTeam getOrThrow(Long id) {
        SysTeam team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "团队不存在");
        }
        return team;
    }

    private TeamDTO toDTO(SysTeam t) {
        return TeamDTO.builder()
                .id(t.getId())
                .parentId(t.getParentId())
                .name(t.getName())
                .type(t.getType())
                .sortOrder(t.getSortOrder())
                .build();
    }
}
