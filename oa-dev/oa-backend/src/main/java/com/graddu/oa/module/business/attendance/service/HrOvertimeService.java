package com.graddu.oa.module.business.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.attendance.dto.OvertimeApplyDTO;
import com.graddu.oa.module.business.attendance.entity.HrOvertime;
import com.graddu.oa.module.business.attendance.vo.OvertimeVO;

/**
 * 加班业务服务接口
 */
public interface HrOvertimeService extends IService<HrOvertime> {

    /**
     * 提交加班申请单
     */
    Long applyOvertime(OvertimeApplyDTO applyDTO);

    /**
     * 获取加班单据详情 (含打印排版数据)
     */
    OvertimeVO getOvertimeDetail(Long id);

    /**
     * 查询当前登录人的加班记录
     */
    PageResult<OvertimeVO> getMyOvertimes(int pageNum, int pageSize);
}
