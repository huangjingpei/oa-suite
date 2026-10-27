package com.graddu.oa.module.business.administrative.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.business.administrative.dto.RedheadDocDTO;
import com.graddu.oa.module.business.administrative.service.AdmRedheadService;
import com.graddu.oa.module.business.administrative.vo.RedheadDocVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "行政红头公文管理接口", description = "企业红头文件拟办、签发流转与公文浏览")
@RestController
@RequestMapping("/adm/redhead")
@RequiredArgsConstructor
public class AdmRedheadController {

    private final AdmRedheadService redheadService;

    @PostMapping("/draft")
    @Operation(summary = "公文拟稿发起 (挂接签发流程)")
    public R<Long> draftDoc(@Valid @RequestBody RedheadDocDTO docDTO) {
        Long id = redheadService.draftDoc(docDTO);
        return R.ok("公文拟稿完成", id);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "获取公文正文与红头排版详情")
    public R<RedheadDocVO> getDetail(@PathVariable("id") Long id) {
        return R.ok(redheadService.getDocDetail(id));
    }

    @GetMapping("/list")
    @Operation(summary = "公文公示列表 (全员可查阅已生效公文)")
    public R<PageResult<RedheadDocVO>> listDocs(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(redheadService.listDocs(pageNum, pageSize));
    }
}
