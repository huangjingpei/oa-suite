package com.graddu.oa.module.business.collab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 通告发布/修改参数
 */
@Data
@Schema(description = "通告公告发布参数")
public class NoticeSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID (新增传空，修改必填)")
    private Long id;

    @NotBlank(message = "通告标题不能为空")
    @Schema(description = "通告标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "通告类型不能为空")
    @Schema(description = "类型 (NOTICE公告, NEWS新闻, SAFETY安全提醒, HOLIDAY休假通知)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String noticeType;

    @NotBlank(message = "正文内容不能为空")
    @Schema(description = "正文HTML/文本内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "附件文件URL")
    private String files;

    @Schema(description = "是否置顶 (1置顶, 0普通)")
    private Integer isTop;

    @Schema(description = "状态 (1已发布, 0草稿)")
    private Integer status;
}
