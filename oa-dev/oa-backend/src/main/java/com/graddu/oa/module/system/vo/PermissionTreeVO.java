package com.graddu.oa.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限树节点展示对象
 */
@Data
@Schema(description = "系统权限树节点视图对象")
public class PermissionTreeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "父级权限ID (0为根)")
    private Long parentId;

    @Schema(description = "权限/菜单名称")
    private String name;

    @Schema(description = "权限标识编码")
    private String perms;

    @Schema(description = "类型 (1:目录, 2:菜单, 3:按钮操作)")
    private Integer menuType;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "状态 (1:正常, 0:停用)")
    private Integer status;

    @Schema(description = "子级节点列表")
    private List<PermissionTreeVO> children = new ArrayList<>();
}
