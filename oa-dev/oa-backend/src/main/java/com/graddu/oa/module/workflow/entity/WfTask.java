package com.graddu.oa.module.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程待办任务实体
 */
@Data
@TableName("oa_wf_task")
public class WfTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long instId;

    private String nodeKey;

    private String nodeName;

    private Integer nodeSeq;

    private String assigneeUsername;

    private String assigneeName;

    private String taskType; // APPROVE, SIGN, COUNTERSIGN, NOTIFY

    private Integer status; // 1待处理, 2已处理, 3已转办/归档

    private LocalDateTime claimTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
