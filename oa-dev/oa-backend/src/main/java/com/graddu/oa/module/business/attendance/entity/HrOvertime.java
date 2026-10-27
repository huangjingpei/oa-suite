package com.graddu.oa.module.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 人事加班申请单实体
 */
@Data
@TableName("oa_hr_overtime")
public class HrOvertime implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String overtimeNo;

    private String applicantUsername;

    private String applicantName;

    private String deptName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal totalHours;

    private String reason;

    private String files;

    private Long procInstId;

    private Integer procStatus;

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
