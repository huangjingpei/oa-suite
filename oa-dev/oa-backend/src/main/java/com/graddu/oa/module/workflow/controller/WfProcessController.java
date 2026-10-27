package com.graddu.oa.module.workflow.controller;

import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.R;
import com.graddu.oa.module.workflow.dto.ProcessAddSignDTO;
import com.graddu.oa.module.workflow.dto.ProcessApproveDTO;
import com.graddu.oa.module.workflow.dto.ProcessRejectDTO;
import com.graddu.oa.module.workflow.dto.ProcessStartDTO;
import com.graddu.oa.module.workflow.entity.WfInstance;
import com.graddu.oa.module.workflow.service.WfEngineService;
import com.graddu.oa.module.workflow.vo.ProcessHistoryVO;
import com.graddu.oa.module.workflow.vo.TaskVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "轻量工作流引擎接口", description = "流程发起、待办/已办、审批同意、驳回、加签与履历跟踪")
@RestController
@RequestMapping("/wf/process")
@RequiredArgsConstructor
public class WfProcessController {

    private final WfEngineService engineService;

    @PostMapping("/start")
    @Operation(summary = "发起审批流程")
    public R<Long> startProcess(@Valid @RequestBody ProcessStartDTO startDTO) {
        Long instId = engineService.startProcess(startDTO);
        return R.ok("流程发起成功", instId);
    }

    @PostMapping("/approve")
    @Operation(summary = "审批同意 (支持加签/自动流转下一节点)")
    public R<Void> approve(@Valid @RequestBody ProcessApproveDTO approveDTO) {
        engineService.approve(approveDTO);
        return R.ok("审批操作成功", null);
    }

    @PostMapping("/reject")
    @Operation(summary = "审批驳回")
    public R<Void> reject(@Valid @RequestBody ProcessRejectDTO rejectDTO) {
        engineService.reject(rejectDTO);
        return R.ok("单据已驳回", null);
    }

    @PostMapping("/add-sign")
    @Operation(summary = "流程动态加签")
    public R<Void> addSign(@Valid @RequestBody ProcessAddSignDTO addSignDTO) {
        engineService.addSign(addSignDTO);
        return R.ok("加签成功", null);
    }

    @PostMapping("/cancel/{instId}")
    @Operation(summary = "流程作废/撤销")
    public R<Void> cancelProcess(@PathVariable("instId") Long instId,
                                @RequestParam(value = "reason", defaultValue = "发起人主动撤销") String reason) {
        engineService.cancelProcess(instId, reason);
        return R.ok("流程已作废", null);
    }

    @GetMapping("/pending")
    @Operation(summary = "查询我的待办审批任务列表 (工作台待办)")
    public R<PageResult<TaskVO>> getPendingTasks(
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @Parameter(description = "条数") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(engineService.getPendingTasks(pageNum, pageSize));
    }

    @GetMapping("/handled")
    @Operation(summary = "查询我的已办审批任务列表 (工作台已办)")
    public R<PageResult<TaskVO>> getHandledTasks(
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @Parameter(description = "条数") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(engineService.getHandledTasks(pageNum, pageSize));
    }

    @GetMapping("/my-submitted")
    @Operation(summary = "查询我发起的审批单据列表")
    public R<PageResult<TaskVO>> getMySubmitted(
            @Parameter(description = "页码") @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @Parameter(description = "条数") @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return R.ok(engineService.getMySubmittedProcesses(pageNum, pageSize));
    }

    @GetMapping("/history/{instId}")
    @Operation(summary = "获取指定流程实例的完整审批履历时间轴")
    public R<List<ProcessHistoryVO>> getProcessHistory(@PathVariable("instId") Long instId) {
        return R.ok(engineService.getProcessHistory(instId));
    }

    @GetMapping("/instance/{instId}")
    @Operation(summary = "获取流程实例详细信息")
    public R<WfInstance> getInstanceDetail(@PathVariable("instId") Long instId) {
        return R.ok(engineService.getProcessInstance(instId));
    }
}
