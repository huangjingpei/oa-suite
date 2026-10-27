package com.graddu.oa.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysDeptMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.system.service.SysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysUserMapper userMapper;

    @Override
    public List<SysDept> listDeptTree() {
        return list(new LambdaQueryWrapper<SysDept>()
                .orderByAsc(SysDept::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDept(SysDept dept) {
        if (dept.getParentId() == null) {
            dept.setParentId(0L);
        }
        if (dept.getDeptCode() != null && !dept.getDeptCode().trim().isEmpty()) {
            long count = count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptCode, dept.getDeptCode()));
            if (count > 0) {
                throw new BusinessException("部门编码 [" + dept.getDeptCode() + "] 已存在");
            }
        } else {
            dept.setDeptCode("DEPT_" + System.currentTimeMillis());
        }
        save(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(SysDept dept) {
        if (dept.getId() == null) {
            throw new BusinessException("部门ID不能为空");
        }
        SysDept exist = getById(dept.getId());
        if (exist == null) {
            throw new BusinessException("部门不存在");
        }
        if (dept.getDeptCode() != null && !dept.getDeptCode().equals(exist.getDeptCode())) {
            long count = count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getDeptCode, dept.getDeptCode()));
            if (count > 0) {
                throw new BusinessException("部门编码 [" + dept.getDeptCode() + "] 已存在");
            }
        }
        updateById(dept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        long childCount = count(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该部门下存在下级子部门，不允许直接删除");
        }
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, id));
        if (userCount > 0) {
            throw new BusinessException("该部门下仍有分配员工，请先调整员工所属部门后再删除");
        }
        removeById(id);
    }
}
