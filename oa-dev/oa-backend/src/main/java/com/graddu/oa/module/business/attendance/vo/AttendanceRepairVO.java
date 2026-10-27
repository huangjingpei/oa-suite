package com.graddu.oa.module.business.attendance.vo;

import com.graddu.oa.module.business.attendance.entity.HrAttendanceRepairDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤补卡视图对象
 */
@Data
@Schema(description = "补卡申请视图对象")
public class AttendanceRepairVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;

    private List<HrAttendanceRepairDetail> details;
}
