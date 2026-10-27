package com.graddu.oa.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.dto.RoleDTO;
import com.graddu.oa.module.system.entity.SysRole;
import com.graddu.oa.module.system.entity.SysUserRole;
import com.graddu.oa.module.system.mapper.SysRoleMapper;
import com.graddu.oa.module.system.mapper.SysUserRoleMapper;
import com.graddu.oa.module.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysUserRoleMapper userRoleMapper;

    @Override
    public List<SysRole> listAllRoles() {
        return list(new LambdaQueryWrapper<SysRole>()
                .orderByAsc(SysRole::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleDTO dto) {
        long count = count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()));
        if (count > 0) {
            throw new BusinessException("角色编码 [" + dto.getRoleCode() + "] 已存在");
        }
        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        save(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("角色ID不能为空");
        }
        SysRole exist = getById(dto.getId());
        if (exist == null) {
            throw new BusinessException("角色不存在");
        }
        if (!exist.getRoleCode().equals(dto.getRoleCode())) {
            long count = count(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, dto.getRoleCode()));
            if (count > 0) {
                throw new BusinessException("角色编码 [" + dto.getRoleCode() + "] 已存在");
            }
        }
        BeanUtils.copyProperties(dto, exist);
        updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = getById(roleId);
        if (role == null) return;
        if ("ROLE_ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("系统内置超级管理员角色不允许删除");
        }
        long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
        if (userCount > 0) {
            throw new BusinessException("该角色下仍绑定有系统员工，请先解绑后再删除");
        }
        removeById(roleId);
    }
}
