package com.graddu.oa.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graddu.oa.module.system.entity.SysDept;

import java.util.List;

public interface SysDeptService extends IService<SysDept> {

    List<SysDept> listDeptTree();
}
