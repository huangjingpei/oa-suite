package com.graddu.oa.module.business.attendance.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.attendance.dto.AttendanceRepairDTO;
import com.graddu.oa.module.business.attendance.dto.ClockInDTO;
import com.graddu.oa.module.business.attendance.service.HrAttendanceService;
import com.graddu.oa.module.business.attendance.vo.AttendanceRepairVO;
import com.graddu.oa.module.business.attendance.vo.ClockRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "考勤打卡与补卡管理接口", description = "员工考勤打卡流水与异常漏卡补卡申请")
@RestController
@RequestMapping("/hr/attendance")
@RequiredArgsConstructor
public class HrAttendanceController {

    private final HrAttendanceService attendanceService;

    @PostMapping("/clock-in")
    @Operation(summary = "考勤打卡 (支持PC端与移动端GPS定位打卡)")
    public R<Void> clockIn(@Valid @RequestBody ClockInDTO clockInDTO) {
        attendanceService.clockIn(clockInDTO);
        return R.ok("打卡成功", null);
    }

    @GetMapping("/my-records")
    @Operation(summary = "查询我的日常考勤打卡流水记录 (分页)")
    public R<PageResult<ClockRecordVO>> getMyClockRecords(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(attendanceService.getMyClockRecords(pageNum, pageSize));
    }

    @PostMapping("/repair/apply")
    @Operation(summary = "提交考勤异常补卡申请 (挂接审批流程)")
    public R<Long> applyRepair(@Valid @RequestBody AttendanceRepairDTO repairDTO) {
        Long id = attendanceService.applyRepair(repairDTO);
        return R.ok("补卡申请提交成功", id);
    }

    @GetMapping("/repair/detail/{id}")
    @Operation(summary = "获取补卡申请详情 (含打印排版数据与漏卡明细)")
    public R<AttendanceRepairVO> getRepairDetail(@PathVariable("id") Long id) {
        return R.ok(attendanceService.getRepairDetail(id));
    }

    @GetMapping("/repair/my")
    @Operation(summary = "查询我的补卡记录 (分页)")
    public R<PageResult<AttendanceRepairVO>> getMyRepairs(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(attendanceService.getMyRepairs(pageNum, pageSize));
    }
}
