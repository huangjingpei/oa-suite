package com.graddu.oa.module.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程运行实例实体
 */
@Data
@TableName("oa_wf_instance")
public class WfInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long procDefId;

    private String procKey;

    private String businessTable;

    private Long businessId;

    private String title;

    private String initiatorUsername;

    private String initiatorName;

    private String deptName;

    private String currentNodeName;

    private String currentAssignees;

    private Integer status; // 0草稿, 1审批中, 2通过, 3驳回, 4撤回

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
