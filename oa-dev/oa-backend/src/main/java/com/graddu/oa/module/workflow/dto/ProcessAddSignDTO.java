package com.graddu.oa.module.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程加签操作传输对象
 */
@Data
@Schema(description = "流程加签请求参数")
public class ProcessAddSignDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "任务ID不能为空")
    @Schema(description = "当前处理的任务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long taskId;

    @NotBlank(message = "加签指派人员不能为空")
    @Schema(description = "被加签人员账号", example = "dev_mgr", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetUsername;

    @Schema(description = "加签说明", example = "请王主管先审阅技术可行性")
    private String opinion;
}
