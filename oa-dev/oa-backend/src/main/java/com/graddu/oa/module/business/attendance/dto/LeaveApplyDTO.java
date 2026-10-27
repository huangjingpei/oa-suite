package com.graddu.oa.module.business.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假申请单提交数据传输对象
 */
@Data
@Schema(description = "请假申请提交对象")
public class LeaveApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "请假类型不能为空")
    @Schema(description = "请假类型 (ANNUAL, PERSONAL, SICK, REST, MARRIAGE, MATERNITY, FUNERAL)", example = "PERSONAL", requiredMode = Schema.RequiredMode.REQUIRED)
    private String leaveType;

    @NotNull(message = "请假开始时间不能为空")
    @Schema(description = "起始时间", example = "2026-10-28 09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @NotNull(message = "请假结束时间不能为空")
    @Schema(description = "结束时间", example = "2026-10-29 18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @NotNull(message = "请假总小时数不能为空")
    @Schema(description = "总工时(小时)", example = "16.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalHours;

    @NotNull(message = "请假折算天数不能为空")
    @Schema(description = "折算天数", example = "2.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalDays;

    @NotBlank(message = "请假事由不能为空")
    @Schema(description = "请假事由详细说明", example = "家中有事处理", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "证明附件文件URL (多文件以逗号分隔)")
    private String files;

    @Schema(description = "是否保存为草稿 (true仅保存草稿, false立即提交发起审批)", defaultValue = "false")
    private Boolean isDraft = false;

    @Schema(description = "动态审批人账号列表 (按顺序: 如 [\"dev_mgr\", \"hr_mgr\"])")
    private List<String> auditUsernames;

    @Schema(description = "知会人账号列表")
    private List<String> notifyUsernames;
}
