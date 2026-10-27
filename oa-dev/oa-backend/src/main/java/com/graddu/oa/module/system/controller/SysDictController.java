package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.entity.SysDictData;
import com.graddu.oa.module.system.entity.SysDictType;
import com.graddu.oa.module.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统数据字典管理接口", description = "字典类型与数据项CRUD")
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;

    // ================= 字典类型 =================

    @GetMapping("/type/list")
    @Operation(summary = "查询所有字典类型列表")
    public R<List<SysDictType>> listTypes() {
        return R.ok(dictService.listAllTypes());
    }

    @PostMapping("/type")
    @Operation(summary = "新增字典类型")
    public R<Boolean> createType(@RequestBody SysDictType type) {
        dictService.createType(type);
        return R.ok(true);
    }

    @PutMapping("/type")
    @Operation(summary = "修改字典类型")
    public R<Boolean> updateType(@RequestBody SysDictType type) {
        dictService.updateType(type);
        return R.ok(true);
    }

    @DeleteMapping("/type/{id}")
    @Operation(summary = "删除字典类型")
    public R<Boolean> deleteType(@PathVariable("id") Long id) {
        dictService.deleteType(id);
        return R.ok(true);
    }

    // ================= 字典数据项 =================

    @GetMapping("/data/type/{dictType}")
    @Operation(summary = "根据字典类型编码查询数据项列表")
    public R<List<SysDictData>> listDataByType(@PathVariable("dictType") String dictType) {
        return R.ok(dictService.listDataByType(dictType));
    }

    @PostMapping("/data")
    @Operation(summary = "新增字典数据项")
    public R<Boolean> createData(@RequestBody SysDictData data) {
        dictService.createData(data);
        return R.ok(true);
    }

    @PutMapping("/data")
    @Operation(summary = "修改字典数据项")
    public R<Boolean> updateData(@RequestBody SysDictData data) {
        dictService.updateData(data);
        return R.ok(true);
    }

    @DeleteMapping("/data/{id}")
    @Operation(summary = "删除字典数据项")
    public R<Boolean> deleteData(@PathVariable("id") Long id) {
        dictService.deleteData(id);
        return R.ok(true);
    }
}
