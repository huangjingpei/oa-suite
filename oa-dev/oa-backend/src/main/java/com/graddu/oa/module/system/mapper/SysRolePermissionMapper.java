package com.graddu.oa.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graddu.oa.module.system.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联数据访问接口
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
