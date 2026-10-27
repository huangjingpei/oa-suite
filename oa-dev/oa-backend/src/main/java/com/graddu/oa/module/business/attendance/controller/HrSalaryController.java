package com.graddu.oa.module.business.attendance.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.attendance.dto.SalaryVerifyDTO;
import com.graddu.oa.module.business.attendance.service.HrSalaryService;
import com.graddu.oa.module.business.attendance.vo.SalaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人事薪酬与工资条安全查询接口", description = "二级密码安全验证、个人月度工资条明细与签收确认")
@RestController
@RequestMapping("/hr/salary")
@RequiredArgsConstructor
public class HrSalaryController {

    private final HrSalaryService salaryService;

    @PostMapping("/verify-pwd")
    @Operation(summary = "验证工资条二级安全查询密码")
    public R<Boolean> verifySalaryPassword(@Valid @RequestBody SalaryVerifyDTO dto) {
        boolean valid = salaryService.verifySalaryPassword(dto);
        return R.ok("安全身份核验通过", valid);
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的月度工资条列表 (严格本人数据隔离)")
    public R<PageResult<SalaryVO>> getMySalaries(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(salaryService.getMySalaries(month, pageNum, pageSize));
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取工资条明细与电子单详情")
    public R<SalaryVO> getSalaryDetail(@PathVariable("id") Long id) {
        return R.ok(salaryService.getSalaryDetail(id));
    }

    @PutMapping("/confirm/{id}")
    @Operation(summary = "员工确认核对无误并在线签收工资条")
    public R<Boolean> confirmSalary(@PathVariable("id") Long id) {
        salaryService.confirmSalary(id);
        return R.ok("工资条已成功签收确认", true);
    }
}
