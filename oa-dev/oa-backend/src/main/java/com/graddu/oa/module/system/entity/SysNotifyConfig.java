package com.graddu.oa.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企微/钉钉/飞书消息提醒机器人配置实体
 */
@Data
@TableName("oa_sys_notify_config")
public class SysNotifyConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 平台类型 (WECHAT_WORK企业微信, DINGTALK钉钉, FEISHU飞书, CUSTOM自定义)
     */
    private String platform;

    /**
     * 机器人名称
     */
    private String name;

    /**
     * Webhook URL
     */
    private String webhookUrl;

    /**
     * 加签密钥 (钉钉/飞书加签)
     */
    private String secretKey;

    /**
     * 启用状态 (1启用, 0禁用)
     */
    private Integer status;

    /**
     * 订阅事件列表 (逗号分隔: TASK_ASSIGNED,PROCESS_FINISHED,NOTICE_PUBLISHED)
     */
    private String notifyEvents;

    /**
     * 备注说明
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
