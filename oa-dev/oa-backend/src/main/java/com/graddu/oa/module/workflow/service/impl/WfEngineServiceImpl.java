package com.graddu.oa.module.workflow.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.ResultCode;
import com.graddu.oa.common.enums.ActionTypeEnum;
import com.graddu.oa.common.enums.ProcStatusEnum;
import com.graddu.oa.common.enums.TaskStatusEnum;
import com.graddu.oa.common.enums.TaskTypeEnum;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.system.service.SysNotifyConfigService;
import com.graddu.oa.module.workflow.dto.ProcessAddSignDTO;
import com.graddu.oa.module.workflow.dto.ProcessApproveDTO;
import com.graddu.oa.module.workflow.dto.ProcessRejectDTO;
import com.graddu.oa.module.workflow.dto.ProcessStartDTO;
import com.graddu.oa.module.workflow.entity.*;
import com.graddu.oa.module.workflow.mapper.*;
import com.graddu.oa.module.workflow.service.WfEngineService;
import com.graddu.oa.module.workflow.vo.ProcessHistoryVO;
import com.graddu.oa.module.workflow.vo.TaskVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自研轻量工作流引擎核心调度服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfEngineServiceImpl implements WfEngineService {

    private final WfDefinitionMapper defMapper;
    private final WfInstanceMapper instMapper;
    private final WfTaskMapper taskMapper;
    private final WfTaskHistoryMapper historyMapper;
    private final WfFreeRouteMapper routeMapper;
    private final SysUserMapper userMapper;
    private final SysNotifyConfigService notifyConfigService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long startProcess(ProcessStartDTO startDTO) {
        // 1. 校验流程定义
        WfDefinition def = defMapper.selectOne(new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getProcKey, startDTO.getProcKey())
                .eq(WfDefinition::getStatus, 1));
        if (def == null) {
            throw new BusinessException(ResultCode.PROCESS_DEF_NOT_FOUND);
        }

        if (CollectionUtils.isEmpty(startDTO.getAuditUsernames())) {
            throw new BusinessException("流程审批人链条不能为空");
        }

        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser currentUser = getUser(currentUsername);

        // 2. 创建流程实例
        WfInstance inst = new WfInstance();
        inst.setProcDefId(def.getId());
        inst.setProcKey(def.getProcKey());
        inst.setBusinessTable(startDTO.getBusinessTable());
        inst.setBusinessId(startDTO.getBusinessId());
        inst.setTitle(startDTO.getTitle());
        inst.setInitiatorUsername(currentUser.getUsername());
        inst.setInitiatorName(currentUser.getRealName());
        inst.setStatus(ProcStatusEnum.RUNNING.getCode());
        inst.setCurrentNodeName("第1级审批");
        inst.setCurrentAssignees(startDTO.getAuditUsernames().get(0));
        inst.setStartTime(LocalDateTime.now());
        instMapper.insert(inst);

        // 3. 存储自由流程路由链条
        try {
            WfFreeRoute route = new WfFreeRoute();
            route.setInstId(inst.getId());
            route.setAuditChainJson(objectMapper.writeValueAsString(startDTO.getAuditUsernames()));
            if (!CollectionUtils.isEmpty(startDTO.getNotifyUsernames())) {
                route.setNotifyChainJson(objectMapper.writeValueAsString(startDTO.getNotifyUsernames()));
            }
            route.setCurrentSeq(1);
            routeMapper.insert(route);
        } catch (Exception e) {
            log.error("序列化流程路由链条失败", e);
            throw new BusinessException("生成流程路由失败: " + e.getMessage());
        }

        // 4. 生成第1级待办任务
        String firstAssignee = startDTO.getAuditUsernames().get(0);
        SysUser assigneeUser = getUser(firstAssignee);

        WfTask task = new WfTask();
        task.setInstId(inst.getId());
        task.setNodeKey("NODE_1");
        task.setNodeName("第1级审批");
        task.setNodeSeq(1);
        task.setAssigneeUsername(assigneeUser.getUsername());
        task.setAssigneeName(assigneeUser.getRealName());
        task.setTaskType(TaskTypeEnum.APPROVE.getCode());
        task.setStatus(TaskStatusEnum.PENDING.getCode());
        task.setCreateTime(LocalDateTime.now());
        taskMapper.insert(task);

        // 异步向企微/钉钉/飞书推送待办提醒卡片
        notifyConfigService.notifyTaskAssigned(inst.getTitle(), def.getProcName(), currentUser.getRealName(),
                task.getNodeName(), task.getAssigneeUsername(), task.getAssigneeName());

        // 5. 记录发起人提交历史
        recordHistory(inst.getId(), task.getId(), "流程发起", 0, currentUser,
                ActionTypeEnum.SUBMIT.getCode(), "发起审批申请", null, null);

        // 6. 同步更新业务单据表
        updateBusinessStatus(startDTO.getBusinessTable(), startDTO.getBusinessId(), inst.getId(), ProcStatusEnum.RUNNING.getCode());

        log.info("流程实例启动成功: instId={}, procKey={}, initiator={}", inst.getId(), def.getProcKey(), currentUser.getUsername());
        return inst.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(ProcessApproveDTO approveDTO) {
        WfTask task = taskMapper.selectById(approveDTO.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (task.getStatus() != TaskStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.TASK_ALREADY_PROCESSED);
        }

        String currentUsername = StpUtil.getLoginIdAsString();
        if (!task.getAssigneeUsername().equals(currentUsername)) {
            throw new BusinessException(ResultCode.NO_TASK_PERMISSION);
        }

        SysUser operator = getUser(currentUsername);
        WfInstance inst = instMapper.selectById(task.getInstId());
        if (inst == null) {
            throw new BusinessException(ResultCode.PROCESS_INST_NOT_FOUND);
        }

        // 1. 完成当前任务
        task.setStatus(TaskStatusEnum.COMPLETED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // 2. 沉淀当前审批同意历史
        String opinion = StringUtils.hasText(approveDTO.getOpinion()) ? approveDTO.getOpinion() : "同意";
        recordHistory(inst.getId(), task.getId(), task.getNodeName(), task.getNodeSeq(), operator,
                ActionTypeEnum.AGREE.getCode(), opinion, null, null);

        // 3. 处理审批中即时加签
        if (StringUtils.hasText(approveDTO.getAddSignUsername())) {
            SysUser addSignUser = getUser(approveDTO.getAddSignUsername());
            WfTask signTask = new WfTask();
            signTask.setInstId(inst.getId());
            signTask.setNodeKey("NODE_SIGN_" + task.getNodeSeq());
            signTask.setNodeName("加签审批 (" + addSignUser.getRealName() + ")");
            signTask.setNodeSeq(task.getNodeSeq());
            signTask.setAssigneeUsername(addSignUser.getUsername());
            signTask.setAssigneeName(addSignUser.getRealName());
            signTask.setTaskType(TaskTypeEnum.SIGN.getCode());
            signTask.setStatus(TaskStatusEnum.PENDING.getCode());
            signTask.setCreateTime(LocalDateTime.now());
            taskMapper.insert(signTask);

            inst.setCurrentAssignees(addSignUser.getUsername());
            inst.setCurrentNodeName(signTask.getNodeName());
            instMapper.updateById(inst);

            recordHistory(inst.getId(), signTask.getId(), task.getNodeName(), task.getNodeSeq(), operator,
                    ActionTypeEnum.ADDSIGN.getCode(), "加签给: " + addSignUser.getRealName(), addSignUser.getUsername(), null);
            return;
        }

        // 4. 处理审批流转到下一节点
        WfFreeRoute route = routeMapper.selectOne(new LambdaQueryWrapper<WfFreeRoute>()
                .eq(WfFreeRoute::getInstId, inst.getId()));

        List<String> auditChain = parseChain(route.getAuditChainJson());
        int nextSeq = route.getCurrentSeq() + 1;

        if (nextSeq <= auditChain.size()) {
            // 还有下一级审批人
            String nextAssignee = auditChain.get(nextSeq - 1);
            SysUser nextUser = getUser(nextAssignee);

            WfTask nextTask = new WfTask();
            nextTask.setInstId(inst.getId());
            nextTask.setNodeKey("NODE_" + nextSeq);
            nextTask.setNodeName("第" + nextSeq + "级审批");
            nextTask.setNodeSeq(nextSeq);
            nextTask.setAssigneeUsername(nextUser.getUsername());
            nextTask.setAssigneeName(nextUser.getRealName());
            nextTask.setTaskType(TaskTypeEnum.APPROVE.getCode());
            nextTask.setStatus(TaskStatusEnum.PENDING.getCode());
            nextTask.setCreateTime(LocalDateTime.now());
            taskMapper.insert(nextTask);

            notifyConfigService.notifyTaskAssigned(inst.getTitle(), inst.getProcKey(), inst.getInitiatorName(),
                    nextTask.getNodeName(), nextTask.getAssigneeUsername(), nextTask.getAssigneeName());

            route.setCurrentSeq(nextSeq);
            route.setUpdateTime(LocalDateTime.now());
            routeMapper.updateById(route);

            inst.setCurrentNodeName("第" + nextSeq + "级审批");
            inst.setCurrentAssignees(nextUser.getUsername());
            instMapper.updateById(inst);
        } else {
            // 流程全部顺利通过并办结
            inst.setStatus(ProcStatusEnum.APPROVED.getCode());
            inst.setCurrentNodeName("流程办结");
            inst.setCurrentAssignees("");
            inst.setEndTime(LocalDateTime.now());
            instMapper.updateById(inst);

            // 更新业务单据状态为 2 (审批通过)
            updateBusinessStatus(inst.getBusinessTable(), inst.getBusinessId(), inst.getId(), ProcStatusEnum.APPROVED.getCode());
            notifyConfigService.notifyProcessFinished(inst.getTitle(), inst.getProcKey(), inst.getInitiatorName(), true);
            log.info("流程已全面办结并生效: instId={}", inst.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(ProcessRejectDTO rejectDTO) {
        WfTask task = taskMapper.selectById(rejectDTO.getTaskId());
        if (task == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        if (task.getStatus() != TaskStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.TASK_ALREADY_PROCESSED);
        }

        String currentUsername = StpUtil.getLoginIdAsString();
        if (!task.getAssigneeUsername().equals(currentUsername)) {
            throw new BusinessException(ResultCode.NO_TASK_PERMISSION);
        }

        SysUser operator = getUser(currentUsername);
        WfInstance inst = instMapper.selectById(task.getInstId());
        if (inst == null) {
            throw new BusinessException(ResultCode.PROCESS_INST_NOT_FOUND);
        }

        // 1. 完成当前任务并标为驳回
        task.setStatus(TaskStatusEnum.COMPLETED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // 2. 沉淀驳回履历
        recordHistory(inst.getId(), task.getId(), task.getNodeName(), task.getNodeSeq(), operator,
                ActionTypeEnum.REJECT.getCode(), rejectDTO.getOpinion(), null, null);

        // 3. 流程实例变更为驳回状态
        inst.setStatus(ProcStatusEnum.REJECTED.getCode());
        inst.setCurrentNodeName("已驳回");
        inst.setCurrentAssignees("");
        inst.setEndTime(LocalDateTime.now());
        instMapper.updateById(inst);

        // 4. 同步业务单据状态为 3 (已驳回)
        updateBusinessStatus(inst.getBusinessTable(), inst.getBusinessId(), inst.getId(), ProcStatusEnum.REJECTED.getCode());
        notifyConfigService.notifyProcessFinished(inst.getTitle(), inst.getProcKey(), inst.getInitiatorName(), false);
        log.warn("流程已驳回: instId={}, operator={}, opinion={}", inst.getId(), operator.getUsername(), rejectDTO.getOpinion());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSign(ProcessAddSignDTO addSignDTO) {
        WfTask task = taskMapper.selectById(addSignDTO.getTaskId());
        if (task == null || task.getStatus() != TaskStatusEnum.PENDING.getCode()) {
            throw new BusinessException("该任务不可执行加签");
        }

        String currentUsername = StpUtil.getLoginIdAsString();
        if (!task.getAssigneeUsername().equals(currentUsername)) {
            throw new BusinessException(ResultCode.NO_TASK_PERMISSION);
        }

        SysUser operator = getUser(currentUsername);
        SysUser targetUser = getUser(addSignDTO.getTargetUsername());
        WfInstance inst = instMapper.selectById(task.getInstId());

        WfTask signTask = new WfTask();
        signTask.setInstId(inst.getId());
        signTask.setNodeKey("NODE_SIGN_" + task.getNodeSeq());
        signTask.setNodeName("加签审批 (" + targetUser.getRealName() + ")");
        signTask.setNodeSeq(task.getNodeSeq());
        signTask.setAssigneeUsername(targetUser.getUsername());
        signTask.setAssigneeName(targetUser.getRealName());
        signTask.setTaskType(TaskTypeEnum.SIGN.getCode());
        signTask.setStatus(TaskStatusEnum.PENDING.getCode());
        signTask.setCreateTime(LocalDateTime.now());
        taskMapper.insert(signTask);
        notifyConfigService.notifyTaskAssigned(inst.getTitle(), inst.getProcKey(), inst.getInitiatorName(),
                signTask.getNodeName(), signTask.getAssigneeUsername(), signTask.getAssigneeName());

        recordHistory(inst.getId(), signTask.getId(), task.getNodeName(), task.getNodeSeq(), operator,
                ActionTypeEnum.ADDSIGN.getCode(), addSignDTO.getOpinion(), targetUser.getUsername(), null);

        inst.setCurrentAssignees(targetUser.getUsername());
        inst.setCurrentNodeName(signTask.getNodeName());
        instMapper.updateById(inst);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(Long instId, String reason) {
        WfInstance inst = instMapper.selectById(instId);
        if (inst == null) {
            throw new BusinessException(ResultCode.PROCESS_INST_NOT_FOUND);
        }

        String currentUsername = StpUtil.getLoginIdAsString();
        if (!inst.getInitiatorUsername().equals(currentUsername) && !StpUtil.hasRole("ROLE_ADMIN")) {
            throw new BusinessException("只有发起人或超级管理员可作废流程");
        }

        SysUser operator = getUser(currentUsername);

        // 作废所有待办任务
        taskMapper.update(null, new LambdaUpdateWrapper<WfTask>()
                .eq(WfTask::getInstId, instId)
                .eq(WfTask::getStatus, TaskStatusEnum.PENDING.getCode())
                .set(WfTask::getStatus, TaskStatusEnum.TRANSFERRED.getCode()));

        inst.setStatus(ProcStatusEnum.CANCELED.getCode());
        inst.setCurrentNodeName("已作废");
        inst.setCurrentAssignees("");
        inst.setEndTime(LocalDateTime.now());
        instMapper.updateById(inst);

        recordHistory(inst.getId(), null, "作废撤回", 0, operator,
                ActionTypeEnum.CANCEL.getCode(), reason, null, null);

        updateBusinessStatus(inst.getBusinessTable(), inst.getBusinessId(), inst.getId(), ProcStatusEnum.CANCELED.getCode());
    }

    @Override
    public PageResult<TaskVO> getPendingTasks(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<WfTask> page = new Page<>(pageNum, pageSize);
        taskMapper.selectPage(page, new LambdaQueryWrapper<WfTask>()
                .eq(WfTask::getAssigneeUsername, currentUsername)
                .eq(WfTask::getStatus, TaskStatusEnum.PENDING.getCode())
                .orderByDesc(WfTask::getCreateTime));

        List<TaskVO> voList = new ArrayList<>();
        for (WfTask task : page.getRecords()) {
            WfInstance inst = instMapper.selectById(task.getInstId());
            if (inst != null) {
                TaskVO vo = new TaskVO();
                BeanUtils.copyProperties(task, vo);
                vo.setTaskId(task.getId());
                vo.setTaskStatus(task.getStatus());
                vo.setProcKey(inst.getProcKey());
                vo.setTitle(inst.getTitle());
                vo.setInitiatorUsername(inst.getInitiatorUsername());
                vo.setInitiatorName(inst.getInitiatorName());
                vo.setDeptName(inst.getDeptName());
                vo.setInstStatus(inst.getStatus());
                vo.setBusinessTable(inst.getBusinessTable());
                vo.setBusinessId(inst.getBusinessId());
                voList.add(vo);
            }
        }

        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageResult<TaskVO> getHandledTasks(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<WfTaskHistory> page = new Page<>(pageNum, pageSize);
        historyMapper.selectPage(page, new LambdaQueryWrapper<WfTaskHistory>()
                .eq(WfTaskHistory::getOperatorUsername, currentUsername)
                .in(WfTaskHistory::getAction, ActionTypeEnum.AGREE.getCode(), ActionTypeEnum.REJECT.getCode(), ActionTypeEnum.ADDSIGN.getCode())
                .orderByDesc(WfTaskHistory::getCreateTime));

        List<TaskVO> voList = new ArrayList<>();
        for (WfTaskHistory history : page.getRecords()) {
            WfInstance inst = instMapper.selectById(history.getInstId());
            if (inst != null) {
                TaskVO vo = new TaskVO();
                vo.setTaskId(history.getTaskId());
                vo.setInstId(history.getInstId());
                vo.setNodeName(history.getNodeName());
                vo.setNodeSeq(history.getNodeSeq());
                vo.setTaskStatus(TaskStatusEnum.COMPLETED.getCode());
                vo.setProcKey(inst.getProcKey());
                vo.setTitle(inst.getTitle());
                vo.setInitiatorUsername(inst.getInitiatorUsername());
                vo.setInitiatorName(inst.getInitiatorName());
                vo.setDeptName(inst.getDeptName());
                vo.setInstStatus(inst.getStatus());
                vo.setBusinessTable(inst.getBusinessTable());
                vo.setBusinessId(inst.getBusinessId());
                vo.setCreateTime(history.getCreateTime());
                voList.add(vo);
            }
        }
        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageResult<TaskVO> getMySubmittedProcesses(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<WfInstance> page = new Page<>(pageNum, pageSize);
        instMapper.selectPage(page, new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getInitiatorUsername, currentUsername)
                .orderByDesc(WfInstance::getCreateTime));

        List<TaskVO> voList = new ArrayList<>();
        for (WfInstance inst : page.getRecords()) {
            TaskVO vo = new TaskVO();
            vo.setInstId(inst.getId());
            vo.setProcKey(inst.getProcKey());
            vo.setTitle(inst.getTitle());
            vo.setInitiatorUsername(inst.getInitiatorUsername());
            vo.setInitiatorName(inst.getInitiatorName());
            vo.setDeptName(inst.getDeptName());
            vo.setNodeName(inst.getCurrentNodeName());
            vo.setInstStatus(inst.getStatus());
            vo.setBusinessTable(inst.getBusinessTable());
            vo.setBusinessId(inst.getBusinessId());
            vo.setCreateTime(inst.getCreateTime());
            voList.add(vo);
        }
        return new PageResult<>(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<ProcessHistoryVO> getProcessHistory(Long instId) {
        List<WfTaskHistory> list = historyMapper.selectList(new LambdaQueryWrapper<WfTaskHistory>()
                .eq(WfTaskHistory::getInstId, instId)
                .orderByAsc(WfTaskHistory::getCreateTime));

        List<ProcessHistoryVO> result = new ArrayList<>();
        for (WfTaskHistory h : list) {
            ProcessHistoryVO vo = new ProcessHistoryVO();
            BeanUtils.copyProperties(h, vo);
            result.add(vo);
        }
        return result;
    }

    @Override
    public WfInstance getProcessInstance(Long instId) {
        return instMapper.selectById(instId);
    }

    private void recordHistory(Long instId, Long taskId, String nodeName, Integer nodeSeq, SysUser operator,
                               String action, String opinion, String targetAssignee, String files) {
        WfTaskHistory history = new WfTaskHistory();
        history.setInstId(instId);
        history.setTaskId(taskId);
        history.setNodeName(nodeName);
        history.setNodeSeq(nodeSeq);
        history.setOperatorUsername(operator.getUsername());
        history.setOperatorName(operator.getRealName());
        history.setAction(action);
        history.setOpinion(opinion);
        history.setTargetAssignee(targetAssignee);
        history.setFiles(files);
        history.setCreateTime(LocalDateTime.now());
        historyMapper.insert(history);
    }

    private void updateBusinessStatus(String tableName, Long businessId, Long instId, int status) {
        if (StringUtils.hasText(tableName) && businessId != null) {
            String sql = "UPDATE " + tableName + " SET proc_inst_id = ?, proc_status = ? WHERE id = ?";
            try {
                jdbcTemplate.update(sql, instId, status, businessId);
            } catch (Exception e) {
                log.warn("动态更新业务表状态失败 (若为测试数据可忽略): table={}, id={}, err={}", tableName, businessId, e.getMessage());
            }
        }
    }

    private SysUser getUser(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("指定用户不存在: " + username);
        }
        return user;
    }

    private List<String> parseChain(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
