package com.graddu.oa.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "员工资料修改参数")
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "员工工号")
    private String workNo;

    @Schema(description = "所属部门ID")
    private Long deptId;

    @Schema(description = "岗位职务")
    private String postName;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "性别 (1男, 2女, 0未知)")
    private Integer gender;

    @Schema(description = "账号状态 (1正常, 0停用)")
    private Integer status;

    @Schema(description = "分配的角色ID列表")
    private List<Long> roleIds;
}
