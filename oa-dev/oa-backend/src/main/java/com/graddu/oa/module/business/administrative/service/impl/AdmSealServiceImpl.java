package com.graddu.oa.module.business.administrative.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.enums.ProcStatusEnum;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.business.administrative.dto.SealApplyDTO;
import com.graddu.oa.module.business.administrative.entity.AdmSealApply;
import com.graddu.oa.module.business.administrative.mapper.AdmSealApplyMapper;
import com.graddu.oa.module.business.administrative.service.AdmSealService;
import com.graddu.oa.module.business.administrative.vo.SealApplyVO;
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
public class AdmSealServiceImpl extends ServiceImpl<AdmSealApplyMapper, AdmSealApply> implements AdmSealService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applySeal(SealApplyDTO applyDTO) {
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

        // 1. 保存用印申请单
        AdmSealApply apply = new AdmSealApply();
        BeanUtils.copyProperties(applyDTO, apply);
        apply.setApplyNo("YZ" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(4));
        apply.setApplicantUsername(user.getUsername());
        apply.setApplicantName(user.getRealName());
        apply.setDeptName(deptName);
        apply.setProcStatus(Boolean.TRUE.equals(applyDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        save(apply);

        // 2. 挂接并启动用印审批流程
        if (!Boolean.TRUE.equals(applyDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_SEAL");
            startDTO.setBusinessTable("oa_adm_seal_apply");
            startDTO.setBusinessId(apply.getId());
            startDTO.setTitle(user.getRealName() + "发起的用印申请 (" + apply.getFileName() + ")");
            startDTO.setAuditUsernames(applyDTO.getAuditUsernames());

            Long instId = workflowService.startProcess(startDTO);
            apply.setProcInstId(instId);
            updateById(apply);
        }

        log.info("用印申请已提交: applyNo={}, fileName={}", apply.getApplyNo(), apply.getFileName());
        return apply.getId();
    }

    @Override
    public SealApplyVO getSealDetail(Long id) {
        AdmSealApply apply = getById(id);
        if (apply == null) {
            throw new BusinessException("用印申请单不存在");
        }
        SealApplyVO vo = new SealApplyVO();
        BeanUtils.copyProperties(apply, vo);
        return vo;
    }

    @Override
    public PageResult<SealApplyVO> getMySealApplies(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<AdmSealApply> page = new Page<>(pageNum, pageSize);
        page(page, new LambdaQueryWrapper<AdmSealApply>()
                .eq(AdmSealApply::getApplicantUsername, currentUsername)
                .orderByDesc(AdmSealApply::getCreateTime));

        List<SealApplyVO> list = new ArrayList<>();
        for (AdmSealApply a : page.getRecords()) {
            SealApplyVO vo = new SealApplyVO();
            BeanUtils.copyProperties(a, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
