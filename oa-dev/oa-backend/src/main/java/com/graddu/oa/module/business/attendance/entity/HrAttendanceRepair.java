package com.graddu.oa.module.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考勤异常补卡申请单实体
 */
@Data
@TableName("oa_hr_attendance_repair")
public class HrAttendanceRepair implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String repairNo;

    private String applicantUsername;

    private String applicantName;

    private String deptName;

    private String reason;

    private String files;

    private Integer totalCount;

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
