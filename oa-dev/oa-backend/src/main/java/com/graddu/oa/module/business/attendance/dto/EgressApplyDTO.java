package com.graddu.oa.module.business.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 外勤出门/外出报备申请单提交参数
 */
@Data
@Schema(description = "外出报备申请提交参数")
public class EgressApplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "外出开始时间不能为空")
    @Schema(description = "外出开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @NotNull(message = "预计返回时间不能为空")
    @Schema(description = "预计返回时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @NotBlank(message = "外出地点或拜访客户单位不能为空")
    @Schema(description = "外出地点或拜访单位", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Schema(description = "同行人员")
    private String companions;

    @NotNull(message = "预计外勤工时不能为空")
    @DecimalMin(value = "0.5", message = "外出工时至少为0.5小时")
    @Schema(description = "外出工时 (小时)", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal totalHours;

    @NotBlank(message = "外勤工作事由不能为空")
    @Schema(description = "外勤工作事由", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    @Schema(description = "附件文档/路线凭证URL")
    private String files;

    @Schema(description = "是否保存为草稿 (true仅存草稿, false提交并流转)")
    private Boolean isDraft;

    @Schema(description = "指定审批人工号/账号列表")
    private List<String> auditUsernames;

    @Schema(description = "抄送知会人员列表")
    private List<String> notifyUsernames;
}
