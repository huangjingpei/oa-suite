package com.graddu.oa.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.module.system.dto.RoleDTO;
import com.graddu.oa.module.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    List<SysRole> listAllRoles();

    void createRole(RoleDTO dto);

    void updateRole(RoleDTO dto);

    void deleteRole(Long roleId);
}
