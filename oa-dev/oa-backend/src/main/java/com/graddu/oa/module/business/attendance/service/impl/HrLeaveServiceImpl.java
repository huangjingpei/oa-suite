package com.graddu.oa.module.business.attendance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.enums.ProcStatusEnum;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.business.attendance.dto.LeaveApplyDTO;
import com.graddu.oa.module.business.attendance.entity.HrLeave;
import com.graddu.oa.module.business.attendance.mapper.HrLeaveMapper;
import com.graddu.oa.module.business.attendance.service.HrLeaveService;
import com.graddu.oa.module.business.attendance.vo.LeaveVO;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysDeptMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.workflow.dto.ProcessStartDTO;
import com.graddu.oa.module.workflow.service.WfEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrLeaveServiceImpl extends ServiceImpl<HrLeaveMapper, HrLeave> implements HrLeaveService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyLeave(LeaveApplyDTO applyDTO) {
        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, currentUsername));
        if (user == null) {
            throw new BusinessException("申请人不存在");
        }

        String deptName = "";
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                deptName = dept.getDeptName();
            }
        }

        // 1. 生成请假业务单据
        HrLeave leave = new HrLeave();
        BeanUtils.copyProperties(applyDTO, leave);
        leave.setLeaveNo("QJ" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(4));
        leave.setApplicantUsername(user.getUsername());
        leave.setApplicantName(user.getRealName());
        leave.setDeptId(user.getDeptId());
        leave.setDeptName(deptName);
        leave.setProcStatus(Boolean.TRUE.equals(applyDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        save(leave);

        // 2. 若非草稿，自动挂接并发起工作流
        if (!Boolean.TRUE.equals(applyDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_LEAVE");
            startDTO.setBusinessTable("oa_hr_leave");
            startDTO.setBusinessId(leave.getId());
            startDTO.setTitle(user.getRealName() + "发起的请假申请 (" + leave.getTotalDays() + "天)");
            startDTO.setAuditUsernames(applyDTO.getAuditUsernames());
            startDTO.setNotifyUsernames(applyDTO.getNotifyUsernames());

            Long instId = workflowService.startProcess(startDTO);
            leave.setProcInstId(instId);
            updateById(leave);
        }

        log.info("请假单提交成功: leaveNo={}, id={}, isDraft={}", leave.getLeaveNo(), leave.getId(), applyDTO.getIsDraft());
        return leave.getId();
    }

    @Override
    public LeaveVO getLeaveDetail(Long id) {
        HrLeave leave = getById(id);
        if (leave == null) {
            throw new BusinessException("请假单不存在");
        }
        LeaveVO vo = new LeaveVO();
        BeanUtils.copyProperties(leave, vo);
        return vo;
    }

    @Override
    public PageResult<LeaveVO> getMyLeaves(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<HrLeave> page = new Page<>(pageNum, pageSize);
        page(page, new LambdaQueryWrapper<HrLeave>()
                .eq(HrLeave::getApplicantUsername, currentUsername)
                .orderByDesc(HrLeave::getCreateTime));

        List<LeaveVO> list = new ArrayList<>();
        for (HrLeave l : page.getRecords()) {
            LeaveVO vo = new LeaveVO();
            BeanUtils.copyProperties(l, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
