package com.graddu.oa.module.business.attendance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.enums.ProcStatusEnum;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.business.attendance.dto.AttendanceRepairDTO;
import com.graddu.oa.module.business.attendance.dto.ClockInDTO;
import com.graddu.oa.module.business.attendance.entity.HrAttendanceRepair;
import com.graddu.oa.module.business.attendance.entity.HrAttendanceRepairDetail;
import com.graddu.oa.module.business.attendance.entity.HrClockRecord;
import com.graddu.oa.module.business.attendance.mapper.HrAttendanceRepairDetailMapper;
import com.graddu.oa.module.business.attendance.mapper.HrAttendanceRepairMapper;
import com.graddu.oa.module.business.attendance.mapper.HrClockRecordMapper;
import com.graddu.oa.module.business.attendance.service.HrAttendanceService;
import com.graddu.oa.module.business.attendance.vo.AttendanceRepairVO;
import com.graddu.oa.module.business.attendance.vo.ClockRecordVO;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrAttendanceServiceImpl implements HrAttendanceService {

    private final HrClockRecordMapper clockRecordMapper;
    private final HrAttendanceRepairMapper repairMapper;
    private final HrAttendanceRepairDetailMapper repairDetailMapper;
    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;
    private final WfEngineService workflowService;

    @Override
    public void clockIn(ClockInDTO clockInDTO) {
        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, currentUsername));
        if (user == null) {
            throw new BusinessException("员工不存在");
        }

        HrClockRecord record = new HrClockRecord();
        BeanUtils.copyProperties(clockInDTO, record);
        record.setUsername(user.getUsername());
        record.setRealName(user.getRealName());
        record.setDeptId(user.getDeptId());
        record.setClockTime(LocalDateTime.now());
        record.setStatus("NORMAL");
        record.setCreateTime(LocalDateTime.now());

        clockRecordMapper.insert(record);
        log.info("员工考勤打卡成功: user={}, type={}, time={}", user.getUsername(), record.getClockType(), record.getClockTime());
    }

    @Override
    public PageResult<ClockRecordVO> getMyClockRecords(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<HrClockRecord> page = new Page<>(pageNum, pageSize);
        clockRecordMapper.selectPage(page, new LambdaQueryWrapper<HrClockRecord>()
                .eq(HrClockRecord::getUsername, currentUsername)
                .orderByDesc(HrClockRecord::getClockTime));

        List<ClockRecordVO> list = new ArrayList<>();
        for (HrClockRecord r : page.getRecords()) {
            ClockRecordVO vo = new ClockRecordVO();
            BeanUtils.copyProperties(r, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyRepair(AttendanceRepairDTO repairDTO) {
        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, currentUsername));
        if (user == null) {
            throw new BusinessException("员工不存在");
        }

        String deptName = "";
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                deptName = dept.getDeptName();
            }
        }

        // 1. 保存补卡主表
        HrAttendanceRepair repair = new HrAttendanceRepair();
        repair.setRepairNo("BK" + DateUtil.format(new Date(), "yyyyMMdd") + RandomUtil.randomNumbers(4));
        repair.setApplicantUsername(user.getUsername());
        repair.setApplicantName(user.getRealName());
        repair.setDeptName(deptName);
        repair.setReason(repairDTO.getReason());
        repair.setFiles(repairDTO.getFiles());
        repair.setTotalCount(repairDTO.getDetails().size());
        repair.setProcStatus(Boolean.TRUE.equals(repairDTO.getIsDraft()) ? ProcStatusEnum.DRAFT.getCode() : ProcStatusEnum.RUNNING.getCode());

        repairMapper.insert(repair);

        // 2. 保存补卡明细项
        for (AttendanceRepairDTO.RepairDetailItem item : repairDTO.getDetails()) {
            HrAttendanceRepairDetail detail = new HrAttendanceRepairDetail();
            BeanUtils.copyProperties(item, detail);
            detail.setRepairId(repair.getId());
            detail.setCreateTime(LocalDateTime.now());
            repairDetailMapper.insert(detail);
        }

        // 3. 挂接并启动流程
        if (!Boolean.TRUE.equals(repairDTO.getIsDraft())) {
            ProcessStartDTO startDTO = new ProcessStartDTO();
            startDTO.setProcKey("PROC_REPAIR");
            startDTO.setBusinessTable("oa_hr_attendance_repair");
            startDTO.setBusinessId(repair.getId());
            startDTO.setTitle(user.getRealName() + "发起的考勤补卡申请 (" + repair.getTotalCount() + "次)");
            startDTO.setAuditUsernames(repairDTO.getAuditUsernames());

            Long instId = workflowService.startProcess(startDTO);
            repair.setProcInstId(instId);
            repairMapper.updateById(repair);
        }

        log.info("补卡申请提交成功: repairNo={}, count={}", repair.getRepairNo(), repair.getTotalCount());
        return repair.getId();
    }

    @Override
    public AttendanceRepairVO getRepairDetail(Long id) {
        HrAttendanceRepair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException("补卡记录不存在");
        }

        AttendanceRepairVO vo = new AttendanceRepairVO();
        BeanUtils.copyProperties(repair, vo);

        List<HrAttendanceRepairDetail> details = repairDetailMapper.selectList(
                new LambdaQueryWrapper<HrAttendanceRepairDetail>().eq(HrAttendanceRepairDetail::getRepairId, id)
        );
        vo.setDetails(details);
        return vo;
    }

    @Override
    public PageResult<AttendanceRepairVO> getMyRepairs(int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<HrAttendanceRepair> page = new Page<>(pageNum, pageSize);
        repairMapper.selectPage(page, new LambdaQueryWrapper<HrAttendanceRepair>()
                .eq(HrAttendanceRepair::getApplicantUsername, currentUsername)
                .orderByDesc(HrAttendanceRepair::getCreateTime));

        List<AttendanceRepairVO> list = new ArrayList<>();
        for (HrAttendanceRepair r : page.getRecords()) {
            AttendanceRepairVO vo = new AttendanceRepairVO();
            BeanUtils.copyProperties(r, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
