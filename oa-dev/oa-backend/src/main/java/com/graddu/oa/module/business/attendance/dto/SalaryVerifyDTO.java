package com.graddu.oa.module.business.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 工资条安全二级验证密码传输对象
 */
@Data
@Schema(description = "工资条安全二级验证请求对象")
public class SalaryVerifyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "安全验证密码不能为空")
    @Schema(description = "工资查询二级安全密码或个人登录密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
