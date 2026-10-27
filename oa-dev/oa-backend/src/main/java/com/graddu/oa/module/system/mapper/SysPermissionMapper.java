package com.graddu.oa.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graddu.oa.module.system.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限数据访问接口
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
}
