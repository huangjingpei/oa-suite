package com.graddu.oa.module.business.collab.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通告已读未读统计矩阵视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通告全员阅读状态追踪统计对象")
public class NoticeReadStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "通告ID")
    private Long noticeId;

    @Schema(description = "通告标题")
    private String title;

    @Schema(description = "企业全员总人数")
    private Integer totalUsers;

    @Schema(description = "已读人数")
    private Integer readCount;

    @Schema(description = "未读人数")
    private Integer unreadCount;

    @Schema(description = "已读阅读率 (百分比数值)")
    private Double readRate;

    @Schema(description = "已读人员名单")
    private List<UserReadItem> readUsers;

    @Schema(description = "未读人员名单")
    private List<UserReadItem> unreadUsers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "人员阅读状态条目")
    public static class UserReadItem implements Serializable {
        private Long userId;
        private String username;
        private String realName;
        private String deptName;
        private LocalDateTime readTime;
    }
}
