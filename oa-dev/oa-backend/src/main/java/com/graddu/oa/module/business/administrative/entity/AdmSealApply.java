package com.graddu.oa.module.business.administrative.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业用印申请单实体
 */
@Data
@TableName("oa_adm_seal_apply")
public class AdmSealApply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String applyNo;

    private String applicantUsername;

    private String applicantName;

    private String deptName;

    private String agentName;

    private String sealType; // COMMON, CONTRACT, LEGAL, FINANCE, DECLARE

    private String fileName;

    private Integer sealCopies;

    private String purpose;

    private String files;

    private BigDecimal contractAmount;

    private String counterparty;

    private Long procInstId;

    private Integer procStatus; // 0草稿, 1审核中, 2通过, 3驳回, 4作废

    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
