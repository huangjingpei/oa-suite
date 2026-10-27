package com.graddu.oa.module.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 自由流程动态流转路由状态实体
 */
@Data
@TableName("oa_wf_free_route")
public class WfFreeRoute implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long instId;

    private String auditChainJson; // 存储结构: ["admin", "dev_mgr", "hr_mgr"]

    private String notifyChainJson; // 知会人列表

    private String addSignJson; // 加签人信息

    private Integer currentSeq;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
