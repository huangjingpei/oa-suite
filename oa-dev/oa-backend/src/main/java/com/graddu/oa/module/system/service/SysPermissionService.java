package com.graddu.oa.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.module.system.dto.RolePermissionDTO;
import com.graddu.oa.module.system.entity.SysPermission;
import com.graddu.oa.module.system.vo.PermissionTreeVO;

import java.util.List;

/**
 * 权限业务逻辑接口
 */
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 获取全量系统权限树形结构
     */
    List<PermissionTreeVO> getPermissionTree();

    /**
     * 获取指定角色已绑定的权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /**
     * 为角色分配权限
     */
    void assignRolePermissions(RolePermissionDTO dto);

    /**
     * 根据用户名获取权限编码字符串列表 (供 Sa-Token 鉴权)
     */
    List<String> getPermsByUsername(String username);
}
