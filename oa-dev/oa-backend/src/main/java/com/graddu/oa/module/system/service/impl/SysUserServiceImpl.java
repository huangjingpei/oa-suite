package com.graddu.oa.module.system.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graddu.oa.common.api.ResultCode;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.dto.LoginDTO;
import com.graddu.oa.module.system.entity.SysDept;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.mapper.SysDeptMapper;
import com.graddu.oa.module.system.mapper.SysUserMapper;
import com.graddu.oa.module.system.service.SysUserService;
import com.graddu.oa.module.system.vo.LoginVO;
import com.graddu.oa.module.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysDeptMapper deptMapper;
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

        // BCrypt 密码匹配校验
        boolean match = BCrypt.checkpw(loginDTO.getPassword(), user.getPassword());
        if (!match) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // Sa-Token 登录认证
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
}
