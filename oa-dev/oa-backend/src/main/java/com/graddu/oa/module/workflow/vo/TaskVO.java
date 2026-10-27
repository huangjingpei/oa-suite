package com.graddu.oa.module.workflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 待办/已办任务视图对象
 */
@Data
@Schema(description = "工作流任务视图对象")
public class TaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "流程实例ID")
    private Long instId;

    @Schema(description = "流程标识Key")
    private String procKey;

    @Schema(description = "流程名称")
    private String procName;

    @Schema(description = "审批标题/摘要")
    private String title;

    @Schema(description = "发起人账号")
    private String initiatorUsername;

    @Schema(description = "发起人姓名")
    private String initiatorName;

    @Schema(description = "发起人部门")
    private String deptName;

    @Schema(description = "当前任务节点名称")
    private String nodeName;

    @Schema(description = "节点序号")
    private Integer nodeSeq;

    @Schema(description = "任务类型 (APPROVE, SIGN, COUNTERSIGN, NOTIFY)")
    private String taskType;

    @Schema(description = "任务状态 (1待处理, 2已处理)")
    private Integer taskStatus;

    @Schema(description = "流程实例整体状态 (1审批中, 2通过, 3驳回, 4作废)")
    private Integer instStatus;

    @Schema(description = "关联业务表名")
    private String businessTable;

    @Schema(description = "关联业务数据ID")
    private Long businessId;

    @Schema(description = "任务到达时间")
    private LocalDateTime createTime;
}
