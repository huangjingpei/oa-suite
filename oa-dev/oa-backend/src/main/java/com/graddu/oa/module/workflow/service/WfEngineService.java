package com.graddu.oa.module.workflow.service;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.workflow.dto.ProcessAddSignDTO;
import com.graddu.oa.module.workflow.dto.ProcessApproveDTO;
import com.graddu.oa.module.workflow.dto.ProcessRejectDTO;
import com.graddu.oa.module.workflow.dto.ProcessStartDTO;
import com.graddu.oa.module.workflow.entity.WfInstance;
import com.graddu.oa.module.workflow.vo.ProcessHistoryVO;
import com.graddu.oa.module.workflow.vo.TaskVO;

import java.util.List;

/**
 * 自研轻量工作流引擎核心调度服务接口
 */
public interface WfEngineService {

    /**
     * 发起并启动一个审批流程实例
     */
    Long startProcess(ProcessStartDTO startDTO);

    /**
     * 审批同意处理 (支持加签/知会与自动扭转下一节点)
     */
    void approve(ProcessApproveDTO approveDTO);

    /**
     * 审批驳回处理 (流程终止并同步业务状态)
     */
    void reject(ProcessRejectDTO rejectDTO);

    /**
     * 流程动态加签
     */
    void addSign(ProcessAddSignDTO addSignDTO);

    /**
     * 发起人撤回/作废流程
     */
    void cancelProcess(Long instId, String reason);

    /**
     * 查询当前登录人的待办任务列表 (支持分页)
     */
    PageResult<TaskVO> getPendingTasks(int pageNum, int pageSize);

    /**
     * 查询当前登录人的已办任务履历列表 (支持分页)
     */
    PageResult<TaskVO> getHandledTasks(int pageNum, int pageSize);

    /**
     * 查询当前登录人发起的流程列表 (支持分页)
     */
    PageResult<TaskVO> getMySubmittedProcesses(int pageNum, int pageSize);

    /**
     * 查询指定流程实例的全部审批履历历史
     */
    List<ProcessHistoryVO> getProcessHistory(Long instId);

    /**
     * 获取指定流程实例详情
     */
    WfInstance getProcessInstance(Long instId);
}
