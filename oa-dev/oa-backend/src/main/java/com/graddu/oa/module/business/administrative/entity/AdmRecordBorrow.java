package com.graddu.oa.module.business.administrative.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案与资料借阅申请实体
 */
@Data
@TableName("oa_adm_record_borrow")
public class AdmRecordBorrow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String borrowNo;

    private String applicantUsername;

    private String applicantName;

    private String deptName;

    private String recordTitle;

    private String borrowType; // READ_ONLY, BORROW_PHYSICAL, COPY

    private Integer copies;

    private LocalDateTime startTime;

    private LocalDateTime expectedReturnTime;

    private LocalDateTime actualReturnTime;

    private String purpose;

    private Integer returnStatus; // 0未借出, 1借出中, 2已归还, 3逾期

    private String files;

    private Long procInstId;

    private Integer procStatus;

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
