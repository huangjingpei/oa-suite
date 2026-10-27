package com.graddu.oa.module.workflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程审批履历与历史日志视图对象
 */
@Data
@Schema(description = "审批历史履历视图对象")
public class ProcessHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "履历ID")
    private Long id;

    @Schema(description = "流程实例ID")
    private Long instId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点序号")
    private Integer nodeSeq;

    @Schema(description = "操作人账号")
    private String operatorUsername;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作动作 (SUBMIT, AGREE, REJECT, ADDSIGN, NOTIFY, CANCEL)")
    private String action;

    @Schema(description = "审批意见")
    private String opinion;

    @Schema(description = "加签/转办目标人")
    private String targetAssignee;

    @Schema(description = "附件材料")
    private String files;

    @Schema(description = "操作发生时间")
    private LocalDateTime createTime;
}
