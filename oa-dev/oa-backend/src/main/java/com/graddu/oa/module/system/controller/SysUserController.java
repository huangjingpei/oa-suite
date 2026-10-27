package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.dto.UserCreateDTO;
import com.graddu.oa.module.system.dto.UserUpdateDTO;
import com.graddu.oa.module.system.service.SysUserService;
import com.graddu.oa.module.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统用户管理接口", description = "用户分页、CRUD、重置密码与人员选择器")
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping("/options")
    @Operation(summary = "获取可选审批人/员工列表 (用于审批人下拉选择)")
    public R<List<UserVO>> listUserOptions() {
        return R.ok(userService.listAllUsers());
    }

    @GetMapping("/address-book")
    @Operation(summary = "企业全员通讯录名录查询")
    public R<List<UserVO>> getAddressBook(
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return R.ok(userService.listAddressBook(deptId, keyword));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询员工用户列表")
    public R<PageResult<UserVO>> pageUsers(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        return R.ok(userService.pageUsers(pageNum, pageSize, keyword, deptId, status));
    }

    @PostMapping
    @Operation(summary = "新增员工账号")
    public R<Boolean> createUser(@Valid @RequestBody UserCreateDTO dto) {
        userService.createUser(dto);
        return R.ok(true);
    }

    @PutMapping
    @Operation(summary = "修改员工资料")
    public R<Boolean> updateUser(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(dto);
        return R.ok(true);
    }

    @PutMapping("/reset-password/{id}")
    @Operation(summary = "重置员工密码为 123456")
    public R<Boolean> resetPassword(@PathVariable("id") Long id) {
        userService.resetPassword(id);
        return R.ok(true);
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "切换员工账号状态")
    public R<Boolean> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        userService.changeStatus(id, status);
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除员工账号")
    public R<Boolean> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return R.ok(true);
    }
}
