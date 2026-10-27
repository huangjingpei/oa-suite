package com.graddu.oa.module.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤补卡明细项实体
 */
@Data
@TableName("oa_hr_attendance_repair_detail")
public class HrAttendanceRepairDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long repairId;

    private LocalDate repairDate;

    private String repairType; // CHECK_IN, CHECK_OUT, ALL

    private LocalDateTime originalSignTime;

    private LocalDateTime repairSignTime;

    private String remark;

    private LocalDateTime createTime;
}
