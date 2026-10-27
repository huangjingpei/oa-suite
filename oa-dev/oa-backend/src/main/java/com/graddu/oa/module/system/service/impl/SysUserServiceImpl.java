package com.graddu.oa.module.system.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.common.api.ResultCode;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.dto.LoginDTO;
import com.graddu.oa.module.system.dto.UserCreateDTO;
import com.graddu.oa.module.system.dto.UserUpdateDTO;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.entity.SysRole;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.entity.SysUserRole;
import com.graddu.oa.module.system.mapper.SysDeptMapper;
import com.graddu.oa.module.system.mapper.SysRoleMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.system.mapper.SysUserRoleMapper;
import com.graddu.oa.module.system.service.SysUserService;
import com.graddu.oa.module.system.vo.LoginVO;
import com.graddu.oa.module.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final StpInterface stpInterface;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        boolean match = BCrypt.checkpw(loginDTO.getPassword(), user.getPassword());
        if (!match) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        StpUtil.login(user.getUsername());
        String tokenValue = StpUtil.getTokenValue();

        String deptName = "";
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                deptName = dept.getDeptName();
            }
        }

        List<String> roles = stpInterface.getRoleList(user.getUsername(), StpUtil.getLoginType());

        return LoginVO.builder()
                .token(tokenValue)
                .tokenName(StpUtil.getTokenName())
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .deptId(user.getDeptId())
                .deptName(deptName)
                .roles(roles)
                .build();
    }

    @Override
    public UserVO getCurrentUserInfo() {
        String username = StpUtil.getLoginIdAsString();
        SysUser user = getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        fillUserRoles(vo, user.getId());
        return vo;
    }

    @Override
    public List<UserVO> listAllUsers() {
        List<SysUser> list = list(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        List<UserVO> result = new ArrayList<>();
        for (SysUser user : list) {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            if (user.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(user.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public PageResult<UserVO> pageUsers(Integer pageNum, Integer pageSize, String keyword, Long deptId, Integer status) {
        Page<SysUser> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getWorkNo, keyword));
        }
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = page(page, wrapper);
        List<UserVO> voList = userPage.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            if (u.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(u.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
            fillUserRoles(vo, u.getId());
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, userPage.getTotal(), userPage.getCurrent(), userPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateDTO dto) {
        long count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("账号 [" + dto.getUsername() + "] 已被占用");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        String rawPwd = (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) ? "123456" : dto.getPassword();
        user.setPassword(BCrypt.hashpw(rawPwd, BCrypt.gensalt()));
        user.setStatus(1);
        save(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            for (Long roleId : dto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateDTO dto) {
        SysUser user = getById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getWorkNo() != null) user.setWorkNo(dto.getWorkNo());
        if (dto.getDeptId() != null) user.setDeptId(dto.getDeptId());
        if (dto.getPostName() != null) user.setPostName(dto.getPostName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        updateById(user);

        if (dto.getRoleIds() != null) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
            for (Long roleId : dto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) return;
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("超级管理员账号 admin 不允许删除");
        }
        removeById(userId);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    private void fillUserRoles(UserVO vo, Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );
        if (userRoles.isEmpty()) {
            vo.setRoleIds(Collections.emptyList());
            vo.setRoleNames(Collections.emptyList());
            return;
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        vo.setRoleIds(roleIds);
        vo.setRoleNames(roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()));
    }
}
