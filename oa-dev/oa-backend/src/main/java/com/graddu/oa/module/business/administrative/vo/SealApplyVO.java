package com.graddu.oa.module.business.administrative.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用印申请单视图对象
 */
@Data
@Schema(description = "企业用印申请视图对象")
public class SealApplyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String applyNo;

    private String applicantUsername;

    private String applicantName;

    private String deptName;

    private String agentName;

    private String sealType;

    private String fileName;

    private Integer sealCopies;

    private String purpose;

    private String files;

    private BigDecimal contractAmount;

    private String counterparty;

    private Long procInstId;

    private Integer procStatus;

    private LocalDateTime createTime;
}
