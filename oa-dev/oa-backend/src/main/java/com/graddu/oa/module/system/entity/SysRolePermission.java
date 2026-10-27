package com.graddu.oa.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色与权限关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oa_sys_role_permission")
public class SysRolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;

    private Long permissionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
