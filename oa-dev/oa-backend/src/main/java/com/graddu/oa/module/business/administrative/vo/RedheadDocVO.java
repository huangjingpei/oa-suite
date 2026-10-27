package com.graddu.oa.module.business.administrative.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 红头公文视图对象
 */
@Data
@Schema(description = "红头公文发文视图对象")
public class RedheadDocVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String docNo;

    private String title;

    private String docType;

    private String securityLevel;

    private String issuerDept;

    private String drafterName;

    private String signerName;

    private String targetZone;

    private String content;

    private String files;

    private LocalDate publishDate;

    private Long procInstId;

    private Integer procStatus;

    private LocalDateTime createTime;
}
