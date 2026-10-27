package com.graddu.oa.module.business.administrative.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.administrative.dto.RecordBorrowDTO;
import com.graddu.oa.module.business.administrative.service.AdmRecordBorrowService;
import com.graddu.oa.module.business.administrative.vo.RecordBorrowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "行政档案借阅管理接口", description = "企业档案与资料借阅审批申请")
@RestController
@RequestMapping("/adm/borrow")
@RequiredArgsConstructor
public class AdmRecordBorrowController {

    private final AdmRecordBorrowService borrowService;

    @PostMapping("/apply")
    @Operation(summary = "提交档案借阅申请 (挂接审批流程)")
    public R<Long> apply(@Valid @RequestBody RecordBorrowDTO borrowDTO) {
        Long id = borrowService.applyBorrow(borrowDTO);
        return R.ok("借阅申请已提交", id);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取借阅单详情 (含打印单据排版数据)")
    public R<RecordBorrowVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(borrowService.getBorrowDetail(id));
    }

    @GetMapping("/my")
    @Operation(summary = "查询我的档案借阅记录 (分页)")
    public R<PageResult<RecordBorrowVO>> getMyBorrows(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(borrowService.getMyBorrows(pageNum, pageSize));
    }
}
