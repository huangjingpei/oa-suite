package com.graddu.oa.module.business.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤补卡申请提交对象
 */
@Data
@Schema(description = "考勤补卡申请参数")
public class AttendanceRepairDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "补卡异常原因说明不能为空")
    @Schema(description = "异常说明/补卡事由", example = "外勤拜访客户，忘记打卡", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "证明材料附件URL")
    private String files;

    @Schema(description = "是否保存为草稿", defaultValue = "false")
    private Boolean isDraft = false;

    @Schema(description = "审批人账号列表")
    private List<String> auditUsernames;

    @NotEmpty(message = "补卡明细项不能为空")
    @Schema(description = "补卡明细列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RepairDetailItem> details;

    @Data
    @Schema(description = "补卡明细项")
    public static class RepairDetailItem implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "漏打卡日期", example = "2026-10-27")
        private LocalDate repairDate;

        @Schema(description = "补卡类型 (CHECK_IN 上班卡, CHECK_OUT 下班卡, ALL 全天)", example = "CHECK_IN")
        private String repairType;

        @Schema(description = "原打卡时间 (若有)")
        private LocalDateTime originalSignTime;

        @Schema(description = "应修正打卡时间", example = "2026-10-27 09:00:00")
        private LocalDateTime repairSignTime;

        @Schema(description = "说明")
        private String remark;
    }
}
