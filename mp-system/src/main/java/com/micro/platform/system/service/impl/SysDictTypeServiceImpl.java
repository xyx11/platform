package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysDictType;
import com.micro.platform.system.mapper.SysDictTypeMapper;
import com.micro.platform.system.service.SysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 字典类型服务实现
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImplX<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public Page<SysDictType> selectDictTypePage(SysDictType dictType, Integer pageNum, Integer pageSize) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
                .like(StringUtils.hasText(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
                .eq(dictType.getStatus() != null, SysDictType::getStatus, dictType.getStatus())
                .orderByDesc(SysDictType::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDictType> selectDictTypeList(SysDictType dictType) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
                .like(StringUtils.hasText(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
                .eq(dictType.getStatus() != null, SysDictType::getStatus, dictType.getStatus())
                .orderByDesc(SysDictType::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}