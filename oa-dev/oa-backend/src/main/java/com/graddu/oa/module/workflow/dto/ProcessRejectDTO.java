package com.graddu.oa.module.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程审批驳回处理传输对象
 */
@Data
@Schema(description = "审批驳回请求参数")
public class ProcessRejectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "当前处理的任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @NotBlank(message = "驳回原因说明不能为空")
    @Schema(description = "驳回原因及说明", example = "资料不齐全，请补充材料后重新提交", requiredMode = Schema.RequiredMode.REQUIRED)
    private String opinion;
}
