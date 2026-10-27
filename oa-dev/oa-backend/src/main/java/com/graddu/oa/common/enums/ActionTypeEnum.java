package com.graddu.oa.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 流程操作动作类型
 */
@Getter
@AllArgsConstructor
public enum ActionTypeEnum {
    SUBMIT("SUBMIT", "发起提交"),
    AGREE("AGREE", "同意"),
    REJECT("REJECT", "驳回"),
    ADDSIGN("ADDSIGN", "加签"),
    NOTIFY("NOTIFY", "知会"),
    CANCEL("CANCEL", "撤回/作废");

    private final String code;
    private final String description;
}
