package com.graddu.oa.module.business.administrative.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 红头公文拟稿发文实体
 */
@Data
@TableName("oa_adm_redhead_doc")
public class AdmRedheadDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String docNo;

    private String title;

    private String docType; // DECISION, NOTICE, CIRCULAR, ORDER

    private String securityLevel; // NORMAL, SECRET, CONFIDENTIAL

    private String issuerDept;

    private String drafterName;

    private String signerName;

    private String targetZone;

    private String content;

    private String files;

    private LocalDate publishDate;

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
