package com.graddu.oa.module.business.attendance.service;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.business.attendance.dto.AttendanceRepairDTO;
import com.graddu.oa.module.business.attendance.dto.ClockInDTO;
import com.graddu.oa.module.business.attendance.vo.AttendanceRepairVO;
import com.graddu.oa.module.business.attendance.vo.ClockRecordVO;

public interface HrAttendanceService {

    void clockIn(ClockInDTO clockInDTO);

    PageResult<ClockRecordVO> getMyClockRecords(int pageNum, int pageSize);

    Long applyRepair(AttendanceRepairDTO repairDTO);

    AttendanceRepairVO getRepairDetail(Long id);

    PageResult<AttendanceRepairVO> getMyRepairs(int pageNum, int pageSize);
}
