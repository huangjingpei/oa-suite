package com.graddu.oa.module.business.administrative.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案借阅申请视图对象
 */
@Data
@Schema(description = "档案借阅申请视图对象")
public class RecordBorrowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String borrowNo;

    private String applicantUsername;

    private String applicantName;

    private String deptName;

    private String recordTitle;

    private String borrowType;

    private Integer copies;

    private LocalDateTime startTime;

    private LocalDateTime expectedReturnTime;

    private LocalDateTime actualReturnTime;

    private String purpose;

    private Integer returnStatus; // 0未借出, 1借出中, 2已归还, 3逾期

    private String files;

    private Long procInstId;

    private Integer procStatus;

    private LocalDateTime createTime;
}
