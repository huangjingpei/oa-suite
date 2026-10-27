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
import com.graddu.oa.module.business.attendance.dto.OvertimeApplyDTO;
import com.graddu.oa.module.business.attendance.entity.HrOvertime;
import com.graddu.oa.module.business.attendance.mapper.HrOvertimeMapper;
import com.graddu.oa.module.business.attendance.service.HrOvertimeService;
import com.graddu.oa.module.business.attendance.vo.OvertimeVO;
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
public class HrOvertimeServiceImpl extends ServiceImpl<HrOvertimeMapper, HrOvertime> implements HrOvertimeService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyOvertime(OvertimeApplyDTO applyDTO) {
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

        // 1. 生成加班单据记录
        HrOvertime overtime = new HrOvertime();
        BeanUtils.copyProperties(applyDTO, overtime);
        overtime.setOvertimeNo("JB" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(4));
        overtime.setApplicantUsername(user.getUsername());
        overtime.setApplicantName(user.getRealName());
        overtime.setDeptName(deptName);
        overtime.setProcStatus(Boolean.TRUE.equals(applyDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        save(overtime);

        // 2. 若非草稿，启动加班审批流
        if (!Boolean.TRUE.equals(applyDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_OVERTIME");
            startDTO.setBusinessTable("oa_hr_overtime");
            startDTO.setBusinessId(overtime.getId());
            startDTO.setTitle(user.getRealName() + "发起的加班申请 (" + overtime.getTotalHours() + "小时)");
            startDTO.setAuditUsernames(applyDTO.getAuditUsernames());
            startDTO.setNotifyUsernames(applyDTO.getNotifyUsernames());

            Long instId = workflowService.startProcess(startDTO);
            overtime.setProcInstId(instId);
            updateById(overtime);
        }

        log.info("加班申请单提交成功: overtimeNo={}, id={}, isDraft={}", overtime.getOvertimeNo(), overtime.getId(), applyDTO.getIsDraft());
        return overtime.getId();
    }

    @Override
    public OvertimeVO getOvertimeDetail(Long id) {
        HrOvertime overtime = getById(id);
        if (overtime == null) {
            throw new BusinessException("加班单不存在");
        }
        OvertimeVO vo = new OvertimeVO();
        BeanUtils.copyProperties(overtime, vo);
        return vo;
    }

    @Override
    public PageResult<OvertimeVO> getMyOvertimes(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<HrOvertime> page = new Page<>(pageNum, pageSize);
        page(page, new LambdaQueryWrapper<HrOvertime>()
                .eq(HrOvertime::getApplicantUsername, currentUsername)
                .orderByDesc(HrOvertime::getCreateTime));

        List<OvertimeVO> list = new ArrayList<>();
        for (HrOvertime o : page.getRecords()) {
            OvertimeVO vo = new OvertimeVO();
            BeanUtils.copyProperties(o, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
