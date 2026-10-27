package com.graddu.oa.module.business.attendance.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.business.attendance.dto.SalaryVerifyDTO;
import com.graddu.oa.module.business.attendance.entity.HrSalary;
import com.graddu.oa.module.business.attendance.mapper.HrSalaryMapper;
import com.graddu.oa.module.business.attendance.service.HrSalaryService;
import com.graddu.oa.module.business.attendance.vo.SalaryVO;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HrSalaryServiceImpl extends ServiceImpl<HrSalaryMapper, HrSalary> implements HrSalaryService {

    private final SysUserMapper userMapper;

    @Override
    public boolean verifySalaryPassword(SalaryVerifyDTO dto) {
        String currentUsername = StpUtil.getLoginIdAsString();
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, currentUsername));
        if (user == null) {
            throw new BusinessException("员工账号不存在");
        }

        // 校验独立安全密码或当前登录密码
        boolean valid = BCrypt.checkpw(dto.getPassword(), user.getPassword());
        if (!valid) {
            throw new BusinessException("安全验证密码错误，请输入正确的个人密码");
        }
        return true;
    }

    @Override
    public PageResult<SalaryVO> getMySalaries(String month, int pageNum, int pageSize) {
        String currentUsername = StpUtil.getLoginIdAsString();
        Page<HrSalary> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<HrSalary> wrapper = new LambdaQueryWrapper<HrSalary>()
                .eq(HrSalary::getUsername, currentUsername);

        if (StringUtils.hasText(month)) {
            wrapper.eq(HrSalary::getSalaryMonth, month.trim());
        }
        wrapper.orderByDesc(HrSalary::getSalaryMonth);

        page(page, wrapper);

        List<SalaryVO> list = new ArrayList<>();
        for (HrSalary s : page.getRecords()) {
            SalaryVO vo = new SalaryVO();
            BeanUtils.copyProperties(s, vo);
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SalaryVO getSalaryDetail(Long id) {
        String currentUsername = StpUtil.getLoginIdAsString();
        HrSalary salary = getById(id);
        if (salary == null) {
            throw new BusinessException("工资条记录不存在");
        }

        // 权限硬隔离：仅允许查看本人工资，非管理员越权阻断
        if (!currentUsername.equals(salary.getUsername()) && !StpUtil.hasRole("ROLE_ADMIN")) {
            throw new BusinessException("无权查看非本人薪资隐私信息");
        }

        SalaryVO vo = new SalaryVO();
        BeanUtils.copyProperties(salary, vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmSalary(Long id) {
        String currentUsername = StpUtil.getLoginIdAsString();
        HrSalary salary = getById(id);
        if (salary == null) {
            throw new BusinessException("工资条记录不存在");
        }

        if (!currentUsername.equals(salary.getUsername())) {
            throw new BusinessException("仅限本人确认签收工资条");
        }

        salary.setIsConfirmed(1);
        salary.setConfirmTime(LocalDateTime.now());
        updateById(salary);
        log.info("员工签收工资条成功: username={}, salaryMonth={}", currentUsername, salary.getSalaryMonth());
    }
}
