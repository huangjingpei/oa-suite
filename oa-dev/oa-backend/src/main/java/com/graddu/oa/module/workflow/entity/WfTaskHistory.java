package com.graddu.oa.module.workflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程审批履历与操作历史实体
 */
@Data
@TableName("oa_wf_task_history")
public class WfTaskHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long instId;

    private Long taskId;

    private String nodeName;

    private Integer nodeSeq;

    private String operatorUsername;

    private String operatorName;

    private String action; // SUBMIT, AGREE, REJECT, ADDSIGN, NOTIFY, CANCEL

    private String opinion;

    private String targetAssignee;

    private String files;

    private LocalDateTime createTime;
}
