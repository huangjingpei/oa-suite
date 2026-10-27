package com.graddu.oa.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审批任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PENDING(1, "待处理"),
    COMPLETED(2, "已处理"),
    TRANSFERRED(3, "已转办/归档");

    private final int code;
    private final String description;
}
