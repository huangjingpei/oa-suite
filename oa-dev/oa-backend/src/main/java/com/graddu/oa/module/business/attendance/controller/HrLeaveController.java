package com.graddu.oa.module.business.attendance.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.attendance.dto.LeaveApplyDTO;
import com.graddu.oa.module.business.attendance.service.HrLeaveService;
import com.graddu.oa.module.business.attendance.vo.LeaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人事请假管理接口", description = "请假申请、请假单据详情与个人请假记录")
@RestController
@RequestMapping("/hr/leave")
@RequiredArgsConstructor
public class HrLeaveController {

    private final HrLeaveService leaveService;

    @PostMapping("/apply")
    @Operation(summary = "提交请假申请 (支持草稿保存与一键启动流程)")
    public R<Long> apply(@Valid @RequestBody LeaveApplyDTO applyDTO) {
        Long id = leaveService.applyLeave(applyDTO);
        return R.ok("请假单提交成功", id);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取请假单详情 (含打印排版数据)")
    public R<LeaveVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(leaveService.getLeaveDetail(id));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的请假记录 (分页)")
    public R<PageResult<LeaveVO>> getMyLeaves(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(leaveService.getMyLeaves(pageNum, pageSize));
    }
}
