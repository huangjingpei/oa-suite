package com.graddu.oa.module.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人事请假申请单实体
 */
@Data
@TableName("oa_hr_leave")
public class HrLeave implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String leaveNo;

    private String applicantUsername;

    private String applicantName;

    private Long deptId;

    private String deptName;

    private String leaveType; // ANNUAL, SICK, PERSONAL, MARRIAGE, MATERNITY, REST, FUNERAL

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal totalHours;

    private BigDecimal totalDays;

    private String reason;

    private String files;

    private Long procInstId;

    private Integer procStatus; // 0草稿, 1审核中, 2通过, 3驳回, 4作废

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
