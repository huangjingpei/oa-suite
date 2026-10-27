package com.graddu.oa.module.system.controller;

import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.service.SysUserService;
import com.graddu.oa.module.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统用户管理接口", description = "用户列表与人员选择器")
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
}
