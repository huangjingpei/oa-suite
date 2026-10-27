package com.graddu.oa.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 个人资料更新请求参数
 */
@Data
@Schema(description = "个人基本资料修改对象")
public class ProfileUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "真实姓名")
    private String realName;

    @Pattern(regexp = "^(1[3-9]\\d{9})?$", message = "手机号码格式不合法")
    @Schema(description = "手机号码")
    private String phone;

    @Email(message = "电子邮箱格式不合法")
    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "性别 (1男, 2女, 0未知)")
    private Integer gender;

    @Schema(description = "头像URL或标识")
    private String avatar;
}
