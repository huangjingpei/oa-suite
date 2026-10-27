package com.graddu.oa.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程审批实例状态枚举
 */
@Getter
@AllArgsConstructor
public enum ProcStatusEnum {
    DRAFT(0, "草稿"),
    RUNNING(1, "流转审批中"),
    APPROVED(2, "审批通过"),
    REJECTED(3, "审批驳回"),
    CANCELED(4, "撤销作废");

    private final int code;
    private final String description;
}
