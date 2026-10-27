package com.graddu.oa.module.business.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.attendance.dto.EgressApplyDTO;
import com.graddu.oa.module.business.attendance.entity.HrEgress;
import com.graddu.oa.module.business.attendance.vo.EgressVO;

/**
 * 外出/外勤报备业务服务接口
 */
public interface HrEgressService extends IService<HrEgress> {

    /**
     * 提交外出报备申请
     */
    Long applyEgress(EgressApplyDTO applyDTO);

    /**
     * 获取外出申请详情 (含打印排版数据)
     */
    EgressVO getEgressDetail(Long id);

    /**
     * 查询当前登录人的外出报备记录
     */
    PageResult<EgressVO> getMyEgresses(int pageNum, int pageSize);
}
