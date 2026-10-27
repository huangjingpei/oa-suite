package com.graddu.oa.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色权限分配参数传输对象
 */
@Data
@Schema(description = "角色授权请求参数")
public class RolePermissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long roleId;

    @Schema(description = "授予的权限ID列表")
    private List<Long> permissionIds;
}
