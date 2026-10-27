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
import com.graddu.oa.module.business.administrative.dto.RecordBorrowDTO;
import com.graddu.oa.module.business.administrative.entity.AdmRecordBorrow;
import com.graddu.oa.module.business.administrative.mapper.AdmRecordBorrowMapper;
import com.graddu.oa.module.business.administrative.service.AdmRecordBorrowService;
import com.graddu.oa.module.business.administrative.vo.RecordBorrowVO;
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
public class AdmRecordBorrowServiceImpl extends ServiceImpl<AdmRecordBorrowMapper, AdmRecordBorrow> implements AdmRecordBorrowService {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyBorrow(RecordBorrowDTO borrowDTO) {
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

        AdmRecordBorrow borrow = new AdmRecordBorrow();
        BeanUtils.copyProperties(borrowDTO, borrow);
        borrow.setBorrowNo("DA" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(4));
        borrow.setApplicantUsername(user.getUsername());
        borrow.setApplicantName(user.getRealName());
        borrow.setDeptName(deptName);
        borrow.setReturnStatus(0); // 未借出
        borrow.setProcStatus(Boolean.TRUE.equals(borrowDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        save(borrow);

        if (!Boolean.TRUE.equals(borrowDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_BORROW");
            startDTO.setBusinessTable("oa_adm_record_borrow");
            startDTO.setBusinessId(borrow.getId());
            startDTO.setTitle(user.getRealName() + "借阅档案: " + borrow.getRecordTitle());
            startDTO.setAuditUsernames(borrowDTO.getAuditUsernames());

            Long instId = workflowService.startProcess(startDTO);
            borrow.setProcInstId(instId);
            updateById(borrow);
        }

        log.info("档案借阅申请提交成功: borrowNo={}, title={}", borrow.getBorrowNo(), borrow.getRecordTitle());
        return borrow.getId();
    }

    @Override
    public RecordBorrowVO getBorrowDetail(Long id) {
        AdmRecordBorrow borrow = getById(id);
        if (borrow == null) {
            throw new BusinessException("档案借阅单不存在");
        }
        RecordBorrowVO vo = new RecordBorrowVO();
        BeanUtils.copyProperties(borrow, vo);
        return vo;
    }

    @Override
    public PageResult<RecordBorrowVO> getMyBorrows(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<AdmRecordBorrow> page = new Page<>(pageNum, pageSize);
        page(page, new LambdaQueryWrapper<AdmRecordBorrow>()
                .eq(AdmRecordBorrow::getApplicantUsername, currentUsername)
                .orderByDesc(AdmRecordBorrow::getCreateTime));

        List<RecordBorrowVO> list = new ArrayList<>();
        for (AdmRecordBorrow b : page.getRecords()) {
            RecordBorrowVO vo = new RecordBorrowVO();
            BeanUtils.copyProperties(b, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
