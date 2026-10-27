package com.graddu.oa.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录成功响应实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录成功响应对象")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Sa-Token 访问令牌 (Token)")
    private String token;

    @Schema(description = "Token 请求头名称 (Authorization)")
    private String tokenName;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "账号")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "用户拥有角色代码列表")
    private List<String> roles;
}
