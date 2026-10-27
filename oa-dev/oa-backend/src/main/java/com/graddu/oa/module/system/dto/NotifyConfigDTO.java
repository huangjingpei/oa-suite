package com.graddu.oa.module.system.dto;

import lombok.Data;

/**
 * 提醒机器人配置入参 DTO
 */
@Data
public class NotifyConfigDTO {
    private Long id;
    private String platform;
    private String name;
    private String webhookUrl;
    private String secretKey;
    private Integer status;
    private String notifyEvents;
    private String remark;
}
