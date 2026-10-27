package com.graddu.oa.module.business.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假单视图展示对象
 */
@Data
@Schema(description = "请假单视图对象")
public class LeaveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String leaveNo;

    private String applicantUsername;

    private String applicantName;

    private Long deptId;

    private String deptName;

    private String leaveType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal totalHours;

    private BigDecimal totalDays;

    private String reason;

    private String files;

    private Long procInstId;

    private Integer procStatus; // 0草稿, 1审核中, 2通过, 3驳回, 4作废

    private LocalDateTime createTime;
}
