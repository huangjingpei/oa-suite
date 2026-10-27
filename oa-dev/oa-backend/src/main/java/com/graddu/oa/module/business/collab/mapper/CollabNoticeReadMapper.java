package com.graddu.oa.module.business.collab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.graddu.oa.module.business.collab.entity.CollabNoticeRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通告已读记录数据访问接口
 */
@Mapper
public interface CollabNoticeReadMapper extends BaseMapper<CollabNoticeRead> {
}
