package com.graddu.oa.module.business.administrative.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 红头公文拟稿发文提交对象
 */
@Data
@Schema(description = "红头公文拟发参数")
public class RedheadDocDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "发文字号不能为空")
    @Schema(description = "发文字号", example = "启航发〔2026〕01号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String docNo;

    @NotBlank(message = "公文标题不能为空")
    @Schema(description = "公文标题", example = "关于2026年度五一国际劳动节放假安排的通知", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "公文类型不能为空")
    @Schema(description = "公文类型 (DECISION 决议, NOTICE 通知, CIRCULAR 通报, ORDER 指令)", example = "NOTICE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String docType;

    @Schema(description = "公文密级 (NORMAL 普件, SECRET 秘密, CONFIDENTIAL 机密)", example = "NORMAL")
    private String securityLevel = "NORMAL";

    @Schema(description = "签发人姓名")
    private String signerName;

    @Schema(description = "受众发布范围", example = "全员")
    private String targetZone;

    @NotBlank(message = "公文正文内容不能为空")
    @Schema(description = "正文富文本/HTML", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "红头盖章正式文件扫描件PDF或附件URL")
    private String files;

    @Schema(description = "印发日期", example = "2026-04-28")
    private LocalDate publishDate;

    @Schema(description = "是否保存为草稿", defaultValue = "false")
    private Boolean isDraft = false;

    @Schema(description = "审批人账号列表")
    private List<String> auditUsernames;
}
