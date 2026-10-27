package com.graddu.oa.module.business.administrative.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.administrative.dto.SealApplyDTO;
import com.graddu.oa.module.business.administrative.service.AdmSealService;
import com.graddu.oa.module.business.administrative.vo.SealApplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "行政印章管理接口", description = "企业公章、合同章、法人章用印审批申请与单据详情")
@RestController
@RequestMapping("/adm/seal")
@RequiredArgsConstructor
public class AdmSealController {

    private final AdmSealService sealService;

    @PostMapping("/apply")
    @Operation(summary = "提交用印申请 (支持草稿保存与挂接审批流程)")
    public R<Long> apply(@Valid @RequestBody SealApplyDTO applyDTO) {
        Long id = sealService.applySeal(applyDTO);
        return R.ok("用印申请已提交", id);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取用印申请单详情 (含高保真打印盖章排版数据)")
    public R<SealApplyVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(sealService.getSealDetail(id));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的用印记录 (分页)")
    public R<PageResult<SealApplyVO>> getMySealApplies(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(sealService.getMySealApplies(pageNum, pageSize));
    }
}
