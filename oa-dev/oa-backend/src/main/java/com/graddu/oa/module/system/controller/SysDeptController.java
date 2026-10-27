package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统部门管理接口", description = "部门架构树维护与CRUD")
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

    @PostMapping
    @Operation(summary = "新增组织部门")
    public R<Boolean> createDept(@RequestBody SysDept dept) {
        deptService.createDept(dept);
        return R.ok(true);
    }

    @PutMapping
    @Operation(summary = "修改组织部门")
    public R<Boolean> updateDept(@RequestBody SysDept dept) {
        deptService.updateDept(dept);
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除组织部门")
    public R<Boolean> deleteDept(@PathVariable("id") Long id) {
        deptService.deleteDept(id);
        return R.ok(true);
    }
}
