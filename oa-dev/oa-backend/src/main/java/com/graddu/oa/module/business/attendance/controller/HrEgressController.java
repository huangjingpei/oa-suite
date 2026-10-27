package com.graddu.oa.module.business.attendance.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.attendance.dto.EgressApplyDTO;
import com.graddu.oa.module.business.attendance.service.HrEgressService;
import com.graddu.oa.module.business.attendance.vo.EgressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人事外出报备管理接口", description = "公干外出申请、单据详情与个人外出台账")
@RestController
@RequestMapping("/hr/egress")
@RequiredArgsConstructor
public class HrEgressController {

    private final HrEgressService egressService;

    @PostMapping("/apply")
    @Operation(summary = "提交外出报备申请 (支持草稿保存与一键启动流程)")
    public R<Long> apply(@Valid @RequestBody EgressApplyDTO applyDTO) {
        Long id = egressService.applyEgress(applyDTO);
        return R.ok("外出报备单提交成功", id);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取外出报备单详情 (含打印排版数据)")
    public R<EgressVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(egressService.getEgressDetail(id));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的外出记录 (分页)")
    public R<PageResult<EgressVO>> getMyEgresses(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(egressService.getMyEgresses(pageNum, pageSize));
    }
}
