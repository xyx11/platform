package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysDictData;
import com.micro.platform.system.mapper.SysDictDataMapper;
import com.micro.platform.system.service.SysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典数据服务实现
 */
@Service
public class SysDictDataServiceImpl extends ServiceImplX<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public Page<SysDictData> selectDictDataPage(SysDictData dictData, Integer pageNum, Integer pageSize) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
                .like(StringUtils.hasText(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel())
                .eq(dictData.getStatus() != null, SysDictData::getStatus, dictData.getStatus())
                .orderByAsc(SysDictData::getSort);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort);
        return baseMapper.selectList(wrapper);
    }
}