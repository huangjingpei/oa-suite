package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统部门管理接口", description = "部门架构树查询")
@RestController
@RequestMapping("/sys/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/tree")
    @Operation(summary = "获取组织架构部门列表")
    public R<List<SysDept>> listDeptTree() {
        return R.ok(deptService.listDeptTree());
    }
}
