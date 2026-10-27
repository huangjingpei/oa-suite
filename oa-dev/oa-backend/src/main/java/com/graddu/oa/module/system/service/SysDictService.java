package com.graddu.oa.module.system.service;

import com.graddu.oa.module.system.entity.SysDictData;
import com.graddu.oa.module.system.entity.SysDictType;

import java.util.List;

public interface SysDictService {

    List<SysDictType> listAllTypes();

    void createType(SysDictType type);

    void updateType(SysDictType type);

    void deleteType(Long id);

    List<SysDictData> listDataByType(String dictType);

    void createData(SysDictData data);

    void updateData(SysDictData data);

    void deleteData(Long id);
}
