package com.graddu.oa.module.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 流程发起传输对象
 */
@Data
@Schema(description = "流程发起请求参数")
public class ProcessStartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "流程标识Key不能为空")
    @Schema(description = "流程定义Key (如 PROC_LEAVE, PROC_SEAL)", example = "PROC_LEAVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String procKey;

    @NotBlank(message = "关联业务表名不能为空")
    @Schema(description = "关联业务表名", example = "oa_hr_leave", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessTable;

    @NotNull(message = "业务数据ID不能为空")
    @Schema(description = "关联业务单据ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long businessId;

    @NotBlank(message = "流程标题不能为空")
    @Schema(description = "流程标题/摘要", example = "张三发起的请假申请 (2天)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "审批人链条列表不能为空")
    @Schema(description = "动态指定的审批人账号链条列表 (有序)", example = "[\"dev_mgr\", \"hr_mgr\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> auditUsernames;

    @Schema(description = "知会人账号列表 (抄送)")
    private List<String> notifyUsernames;
}
