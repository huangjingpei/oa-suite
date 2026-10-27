package com.graddu.oa.module.business.collab.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 协同通告视图展示对象
 */
@Data
@Schema(description = "通告公告视图对象")
public class NoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "通告类型")
    private String noticeType;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "附件文档URL")
    private String files;

    @Schema(description = "是否置顶 (1置顶, 0普通)")
    private Integer isTop;

    @Schema(description = "状态 (1已发布, 0草稿, 2已下线)")
    private Integer status;

    @Schema(description = "发布人姓名")
    private String publisherName;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "浏览阅读总人次")
    private Integer viewCount;

    @Schema(description = "当前登录人是否已读 (true已读, false未读)")
    private Boolean hasRead;

    @Schema(description = "已读人数")
    private Integer readCount;

    @Schema(description = "未读人数")
    private Integer unreadCount;
}
