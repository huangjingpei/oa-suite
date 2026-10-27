package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.dto.RoleDTO;
import com.graddu.oa.module.system.entity.SysRole;
import com.graddu.oa.module.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统角色管理接口", description = "角色CRUD与权限绑定")
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping("/list")
    @Operation(summary = "获取所有角色列表")
    public R<List<SysRole>> listRoles() {
        return R.ok(roleService.listAllRoles());
    }

    @PostMapping
    @Operation(summary = "新增系统角色")
    public R<Boolean> createRole(@Valid @RequestBody RoleDTO dto) {
        roleService.createRole(dto);
        return R.ok(true);
    }

    @PutMapping
    @Operation(summary = "修改系统角色")
    public R<Boolean> updateRole(@Valid @RequestBody RoleDTO dto) {
        roleService.updateRole(dto);
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统角色")
    public R<Boolean> deleteRole(@PathVariable("id") Long id) {
        roleService.deleteRole(id);
        return R.ok(true);
    }
}
