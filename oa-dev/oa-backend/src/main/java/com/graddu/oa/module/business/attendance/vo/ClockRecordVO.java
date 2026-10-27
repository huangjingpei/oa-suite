package com.graddu.oa.module.business.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考勤打卡流水视图对象
 */
@Data
@Schema(description = "员工打卡流水视图对象")
public class ClockRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String realName;

    private LocalDateTime clockTime;

    private String clockType; // ON, OFF

    private String deviceType;

    private String locationAddress;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String status; // NORMAL, LATE, EARLY, OUTSIDE

    private String remark;

    private LocalDateTime createTime;
}
