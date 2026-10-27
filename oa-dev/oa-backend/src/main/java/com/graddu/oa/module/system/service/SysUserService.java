package com.graddu.oa.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.common.api.PageResult;
import com.graddu.oa.module.system.dto.LoginDTO;
import com.graddu.oa.module.system.dto.PasswordUpdateDTO;
import com.graddu.oa.module.system.dto.ProfileUpdateDTO;
import com.graddu.oa.module.system.dto.UserCreateDTO;
import com.graddu.oa.module.system.dto.UserUpdateDTO;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.vo.LoginVO;
import com.graddu.oa.module.system.vo.UserVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    LoginVO login(LoginDTO loginDTO);

    UserVO getCurrentUserInfo();

    void updatePassword(PasswordUpdateDTO dto);

    void updateProfile(ProfileUpdateDTO dto);

    List<UserVO> listAllUsers();

    PageResult<UserVO> pageUsers(Integer pageNum, Integer pageSize, String keyword, Long deptId, Integer status);

    void createUser(UserCreateDTO dto);

    void updateUser(UserUpdateDTO dto);

    void resetPassword(Long userId);

    void changeStatus(Long userId, Integer status);

    void deleteUser(Long userId);
}
