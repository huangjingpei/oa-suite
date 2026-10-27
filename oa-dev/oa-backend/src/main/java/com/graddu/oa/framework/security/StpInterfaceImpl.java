package com.graddu.oa.framework.security;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graddu.oa.module.system.entity.SysRole;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.entity.SysUserRole;
import com.graddu.oa.module.system.mapper.SysRoleMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.system.mapper.SysUserRoleMapper;
import com.graddu.oa.module.system.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限数据加载接口实现
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionService permissionService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        return permissionService.getPermsByUsername(loginId.toString());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String username = loginId.toString();
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
        return roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }
}
