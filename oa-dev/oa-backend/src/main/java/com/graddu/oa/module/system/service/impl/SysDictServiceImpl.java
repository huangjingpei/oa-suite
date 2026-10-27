package com.graddu.oa.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graddu.oa.common.exception.BusinessException;
import com.graddu.oa.module.system.entity.SysDictData;
import com.graddu.oa.module.system.entity.SysDictType;
import com.graddu.oa.module.system.mapper.SysDictDataMapper;
import com.graddu.oa.module.system.mapper.SysDictTypeMapper;
import com.graddu.oa.module.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl implements SysDictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    @Override
    public List<SysDictType> listAllTypes() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .orderByDesc(SysDictType::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createType(SysDictType type) {
        long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, type.getDictType()));
        if (count > 0) {
            throw new BusinessException("字典类型编码 [" + type.getDictType() + "] 已存在");
        }
        if (type.getStatus() == null) type.setStatus(1);
        dictTypeMapper.insert(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(SysDictType type) {
        SysDictType exist = dictTypeMapper.selectById(type.getId());
        if (exist == null) {
            throw new BusinessException("字典类型不存在");
        }
        if (!exist.getDictType().equals(type.getDictType())) {
            long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                    .eq(SysDictType::getDictType, type.getDictType()));
            if (count > 0) {
                throw new BusinessException("字典类型编码 [" + type.getDictType() + "] 已存在");
            }
        }
        dictTypeMapper.updateById(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type == null) return;
        long dataCount = dictDataMapper.selectCount(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, type.getDictType()));
        if (dataCount > 0) {
            throw new BusinessException("该字典类型下存在字典数据项，请先删除数据项");
        }
        dictTypeMapper.deleteById(id);
    }

    @Override
    public List<SysDictData> listDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createData(SysDictData data) {
        if (data.getStatus() == null) data.setStatus(1);
        if (data.getSortOrder() == null) data.setSortOrder(0);
        dictDataMapper.insert(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateData(SysDictData data) {
        dictDataMapper.updateById(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteData(Long id) {
        dictDataMapper.deleteById(id);
    }
}
