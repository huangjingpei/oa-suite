package com.graddu.oa.module.business.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 外出报备单视图展示对象
 */
@Data
@Schema(description = "外出报备申请单视图对象")
public class EgressVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "外出单流水号")
    private String egressNo;

    @Schema(description = "申请人账号")
    private String applicantUsername;

    @Schema(description = "申请人姓名")
    private String applicantName;

    @Schema(description = "所属部门")
    private String deptName;

    @Schema(description = "外出开始时间")
    private LocalDateTime startTime;

    @Schema(description = "预计返回时间")
    private LocalDateTime endTime;

    @Schema(description = "外出地点或拜访单位")
    private String location;

    @Schema(description = "同行人员")
    private String companions;

    @Schema(description = "核算工时 (小时)")
    private BigDecimal totalHours;

    @Schema(description = "外勤事由")
    private String reason;

    @Schema(description = "附件文档/凭证URL")
    private String files;

    @Schema(description = "工作流实例ID")
    private Long procInstId;

    @Schema(description = "流程审批状态 (0:草稿, 1:审核中, 2:已通过, 3:已驳回, 4:已作废)")
    private Integer procStatus;

    @Schema(description = "单据创建时间")
    private LocalDateTime createTime;
}
