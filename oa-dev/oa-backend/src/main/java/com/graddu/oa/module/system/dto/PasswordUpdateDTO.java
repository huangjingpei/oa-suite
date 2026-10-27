package com.graddu.oa.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 个人修改密码请求参数
 */
@Data
@Schema(description = "个人修改密码传输对象")
public class PasswordUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "当前原密码不能为空")
    @Schema(description = "当前原密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度需在6-32位之间")
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "确认新密码不能为空")
    @Schema(description = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}
