package com.graddu.oa.module.business.attendance.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.attendance.dto.OvertimeApplyDTO;
import com.graddu.oa.module.business.attendance.service.HrOvertimeService;
import com.graddu.oa.module.business.attendance.vo.OvertimeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人事加班申报管理接口", description = "加班申请、加班单据详情与个人加班台账")
@RestController
@RequestMapping("/hr/overtime")
@RequiredArgsConstructor
public class HrOvertimeController {

    private final HrOvertimeService overtimeService;

    @PostMapping("/apply")
    @Operation(summary = "提交加班申报 (支持草稿保存与一键启动流程)")
    public R<Long> apply(@Valid @RequestBody OvertimeApplyDTO applyDTO) {
        Long id = overtimeService.applyOvertime(applyDTO);
        return R.ok("加班申请提交成功", id);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取加班单详情 (含打印排版数据)")
    public R<OvertimeVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(overtimeService.getOvertimeDetail(id));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的加班记录 (分页)")
    public R<PageResult<OvertimeVO>> getMyOvertimes(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(overtimeService.getMyOvertimes(pageNum, pageSize));
    }
}
