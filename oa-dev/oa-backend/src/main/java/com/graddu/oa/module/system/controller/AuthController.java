package com.graddu.oa.module.system.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.system.dto.LoginDTO;
import com.graddu.oa.module.system.service.SysUserService;
import com.graddu.oa.module.system.vo.LoginVO;
import com.graddu.oa.module.system.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统认证接口", description = "登录、注销与当前登录人档案")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    @SaIgnore
    @PostMapping("/login")
    @Operation(summary = "用户账号密码登录")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return R.ok("登录成功", loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public R<Void> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return R.ok("已安全退出", null);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前登录用户信息")
    public R<UserVO> getProfile() {
        return R.ok(userService.getCurrentUserInfo());
    }
}
