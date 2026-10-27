package com.graddu.oa.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工作流任务类型枚举
 */
@Getter
@AllArgsConstructor
public enum TaskTypeEnum {
    APPROVE("APPROVE", "常规审批"),
    SIGN("SIGN", "加签任务"),
    COUNTERSIGN("COUNTERSIGN", "会签任务"),
    NOTIFY("NOTIFY", "知会抄送");

    private final String code;
    private final String name;
}
