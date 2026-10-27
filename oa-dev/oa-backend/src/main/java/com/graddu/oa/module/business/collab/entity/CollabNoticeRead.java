package com.graddu.oa.module.business.collab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通告已读未读追踪实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("oa_collab_notice_read")
public class CollabNoticeRead implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long noticeId;

    private Long userId;

    private String username;

    private String realName;

    private Integer readStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime readTime;
}
