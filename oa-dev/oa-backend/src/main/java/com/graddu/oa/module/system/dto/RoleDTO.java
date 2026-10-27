package com.graddu.oa.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "角色信息提交参数")
public class RoleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID (修改时必填)")
    private Long id;

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "ROLE_DEV", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "研发工程师", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @Schema(description = "角色描述说明")
    private String description;

    @Schema(description = "显示顺序", defaultValue = "0")
    private Integer sortOrder = 0;

    @Schema(description = "状态 (1正常, 0停用)", defaultValue = "1")
    private Integer status = 1;
}
