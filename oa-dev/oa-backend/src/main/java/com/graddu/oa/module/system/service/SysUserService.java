package com.graddu.oa.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.module.system.dto.LoginDTO;
import com.graddu.oa.module.system.entity.SysUser;
import com.graddu.oa.module.system.vo.LoginVO;
import com.graddu.oa.module.system.vo.UserVO;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    LoginVO login(LoginDTO loginDTO);

    UserVO getCurrentUserInfo();

    List<UserVO> listAllUsers();
}
