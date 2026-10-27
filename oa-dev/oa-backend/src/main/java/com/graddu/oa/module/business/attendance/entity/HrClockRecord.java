package com.graddu.oa.module.business.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工打卡流水记录实体
 */
@Data
@TableName("oa_hr_clock_record")
public class HrClockRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;

    private String realName;

    private Long deptId;

    private LocalDateTime clockTime;

    private String clockType; // ON, OFF

    private String deviceType; // MOBILE, WEB, HARDWARE

    private String locationAddress;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String status; // NORMAL, LATE, EARLY, OUTSIDE

    private String remark;

    private LocalDateTime createTime;
}
