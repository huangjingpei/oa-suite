package com.graddu.oa.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务统一返回状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "账号未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求资源不存在"),
    ERROR(500, "系统繁忙，请稍后再试"),

    // 业务状态码
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "账号或密码错误"),
    USER_DISABLED(1003, "账号已被冻结"),

    // 流程引擎状态码
    PROCESS_DEF_NOT_FOUND(2001, "流程定义不存在"),
    PROCESS_INST_NOT_FOUND(2002, "流程实例不存在"),
    TASK_NOT_FOUND(2003, "审批任务不存在"),
    TASK_ALREADY_PROCESSED(2004, "当前任务已被处理"),
    NO_TASK_PERMISSION(2005, "您不是该任务的指派审批人，无权审批");

    private final int code;
    private final String message;
}
