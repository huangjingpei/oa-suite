package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.dto.RolePermissionDTO;
import com.graddu.oa.module.system.service.SysPermissionService;
import com.graddu.oa.module.system.vo.PermissionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统权限与菜单管理接口", description = "权限树查询与角色授权分配")
@RestController
@RequestMapping("/sys/permission")
@RequiredArgsConstructor
public class SysPermissionController {

    private final SysPermissionService permissionService;

    @GetMapping("/tree")
    @Operation(summary = "获取全量权限树形结构")
    public R<List<PermissionTreeVO>> getPermissionTree() {
        return R.ok(permissionService.getPermissionTree());
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色已分配的权限ID列表")
    public R<List<Long>> getRolePermissionIds(@PathVariable("roleId") Long roleId) {
        return R.ok(permissionService.getPermissionIdsByRoleId(roleId));
    }

    @PostMapping("/assign")
    @Operation(summary = "为角色分配细粒度操作权限")
    public R<Boolean> assignRolePermissions(@Valid @RequestBody RolePermissionDTO dto) {
        permissionService.assignRolePermissions(dto);
        return R.ok("分配角色权限成功", true);
    }
}
