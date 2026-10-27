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
import com.graddu.oa.module.business.attendance.dto.EgressApplyDTO;
import com.graddu.oa.module.business.attendance.entity.HrEgress;
import com.graddu.oa.module.business.attendance.mapper.HrEgressMapper;
import com.graddu.oa.module.business.attendance.service.HrEgressService;
import com.graddu.oa.module.business.attendance.vo.EgressVO;
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
public class HrEgressServiceImpl extends ServiceImpl<HrEgressMapper, HrEgress> implements HrEgressService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyEgress(EgressApplyDTO applyDTO) {
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

        // 1. 生成外出报备单据
        HrEgress egress = new HrEgress();
        BeanUtils.copyProperties(applyDTO, egress);
        egress.setEgressNo("WC" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(4));
        egress.setApplicantUsername(user.getUsername());
        egress.setApplicantName(user.getRealName());
        egress.setDeptName(deptName);
        egress.setProcStatus(Boolean.TRUE.equals(applyDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        save(egress);

        // 2. 若非草稿，启动外出审批流
        if (!Boolean.TRUE.equals(applyDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_EGRESS");
            startDTO.setBusinessTable("oa_hr_egress");
            startDTO.setBusinessId(egress.getId());
            startDTO.setTitle(user.getRealName() + "发起的外部公干/外出报备 (" + egress.getLocation() + ")");
            startDTO.setAuditUsernames(applyDTO.getAuditUsernames());
            startDTO.setNotifyUsernames(applyDTO.getNotifyUsernames());

            Long instId = workflowService.startProcess(startDTO);
            egress.setProcInstId(instId);
            updateById(egress);
        }

        log.info("外出报备单提交成功: egressNo={}, id={}, isDraft={}", egress.getEgressNo(), egress.getId(), applyDTO.getIsDraft());
        return egress.getId();
    }

    @Override
    public EgressVO getEgressDetail(Long id) {
        HrEgress egress = getById(id);
        if (egress == null) {
            throw new BusinessException("外出报备单不存在");
        }
        EgressVO vo = new EgressVO();
        BeanUtils.copyProperties(egress, vo);
        return vo;
    }

    @Override
    public PageResult<EgressVO> getMyEgresses(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<HrEgress> page = new Page<>(pageNum, pageSize);
        page(page, new LambdaQueryWrapper<HrEgress>()
                .eq(HrEgress::getApplicantUsername, currentUsername)
                .orderByDesc(HrEgress::getCreateTime));

        List<EgressVO> list = new ArrayList<>();
        for (HrEgress e : page.getRecords()) {
            EgressVO vo = new EgressVO();
            BeanUtils.copyProperties(e, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
