package com.graddu.oa.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.dto.RolePermissionDTO;
import com.graddu.oa.module.system.entity.SysPermission;
import com.graddu.oa.module.system.entity.SysRole;
import com.graddu.oa.module.system.entity.SysRolePermission;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.entity.SysUserRole;
import com.graddu.oa.module.system.mapper.SysPermissionMapper;
import com.graddu.oa.module.system.mapper.SysRoleMapper;
import com.graddu.oa.module.system.mapper.SysRolePermissionMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.system.mapper.SysUserRoleMapper;
import com.graddu.oa.module.system.service.SysPermissionService;
import com.graddu.oa.module.system.vo.PermissionTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限业务逻辑实现类
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        List<SysPermission> list = list(new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSortOrder));

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<PermissionTreeVO> voList = list.stream().map(p -> {
            PermissionTreeVO vo = new PermissionTreeVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).collect(Collectors.toList());

        // 按 parentId 分组构建树形结构
        Map<Long, List<PermissionTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(PermissionTreeVO::getParentId));

        List<PermissionTreeVO> rootNodes = parentMap.getOrDefault(0L, Collections.emptyList());
        buildChildrenTree(rootNodes, parentMap);
        return rootNodes;
    }

    private void buildChildrenTree(List<PermissionTreeVO> nodes, Map<Long, List<PermissionTreeVO>> parentMap) {
        for (PermissionTreeVO node : nodes) {
            List<PermissionTreeVO> children = parentMap.getOrDefault(node.getId(), new ArrayList<>());
            node.setChildren(children);
            if (!children.isEmpty()) {
                buildChildrenTree(children, parentMap);
            }
        }
    }

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        List<SysRolePermission> list = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        return list.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(RolePermissionDTO dto) {
        Long roleId = dto.getRoleId();
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("目标角色不存在");
        }

        // 1. 删除原有关联
        rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );

        // 2. 批量新增新权限
        List<Long> permIds = dto.getPermissionIds();
        if (permIds != null && !permIds.isEmpty()) {
            Set<Long> distinctIds = new HashSet<>(permIds);
            for (Long pid : distinctIds) {
                SysRolePermission rp = SysRolePermission.builder()
                        .roleId(roleId)
                        .permissionId(pid)
                        .createTime(LocalDateTime.now())
                        .build();
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<String> getPermsByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return Collections.emptyList();
        }
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            return Collections.emptyList();
        }

        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId())
        );
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);

        // 超级管理员拥有全权限
        boolean isAdmin = roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleCode()));
        if (isAdmin) {
            List<SysPermission> allPerms = list(new LambdaQueryWrapper<SysPermission>()
                    .eq(SysPermission::getStatus, 1));
            Set<String> permCodes = allPerms.stream()
                    .map(SysPermission::getPerms)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
            permCodes.add("*");
            return new ArrayList<>(permCodes);
        }

        // 普通角色查询绑定的权限
        List<SysRolePermission> rolePerms = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
        );
        if (rolePerms.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> permIds = rolePerms.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
        List<SysPermission> permissions = listByIds(permIds);
        return permissions.stream()
                .filter(p -> p.getStatus() == 1 && StringUtils.hasText(p.getPerms()))
                .map(SysPermission::getPerms)
                .distinct()
                .collect(Collectors.toList());
    }
}
