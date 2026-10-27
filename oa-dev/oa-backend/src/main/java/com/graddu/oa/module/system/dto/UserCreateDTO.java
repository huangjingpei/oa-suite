package com.graddu.oa.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "员工新增参数")
public class UserCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "登录账号不能为空")
    @Schema(description = "登录账号", example = "wangwu", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", example = "王五", requiredMode = Schema.RequiredMode.REQUIRED)
    private String realName;

    @Schema(description = "初始登录密码 (未提供时默认123456)")
    private String password;

    @Schema(description = "员工工号", example = "OA1005")
    private String workNo;

    @Schema(description = "所属部门ID", example = "1002")
    private Long deptId;

    @Schema(description = "岗位职务", example = "高级开发工程师")
    private String postName;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "性别 (1男, 2女, 0未知)", defaultValue = "1")
    private Integer gender = 1;

    @Schema(description = "分配的角色ID列表")
    private List<Long> roleIds;
}
