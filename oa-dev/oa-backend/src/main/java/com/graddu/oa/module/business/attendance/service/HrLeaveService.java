package com.graddu.oa.module.business.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.attendance.dto.LeaveApplyDTO;
import com.graddu.oa.module.business.attendance.entity.HrLeave;
import com.graddu.oa.module.business.attendance.vo.LeaveVO;

public interface HrLeaveService extends IService<HrLeave> {

    Long applyLeave(LeaveApplyDTO applyDTO);

    LeaveVO getLeaveDetail(Long id);

    PageResult<LeaveVO> getMyLeaves(int pageNum, int pageSize);
}
