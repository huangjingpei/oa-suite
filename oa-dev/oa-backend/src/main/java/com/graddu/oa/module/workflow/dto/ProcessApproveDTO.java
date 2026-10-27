package com.graddu.oa.module.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程审批同意处理传输对象
 */
@Data
@Schema(description = "审批同意请求参数")
public class ProcessApproveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "当前处理的任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @Schema(description = "审批意见", example = "同意申请")
    private String opinion;

    @Schema(description = "审批过程中临时加签人账号 (若有)")
    private String addSignUsername;

    @Schema(description = "审批过程中临时知会人账号 (若有)")
    private String notifyUsername;
}
