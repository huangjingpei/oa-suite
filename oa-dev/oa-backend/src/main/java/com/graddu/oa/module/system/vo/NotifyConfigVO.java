package com.graddu.oa.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒机器人配置返参 VO
 */
@Data
public class NotifyConfigVO {
    private Long id;
    private String platform;
    private String name;
    private String webhookUrl;
    private String secretKey;
    private Integer status;
    private String notifyEvents;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
