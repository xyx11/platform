package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.redis.util.RedisUtil;
import com.micro.platform.system.entity.SysDictData;
import com.micro.platform.system.mapper.SysDictDataMapper;
import com.micro.platform.system.service.SysDictDataService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 字典数据服务实现
 */
@Service
public class SysDictDataServiceImpl extends ServiceImplX<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private static final String DICT_CACHE_PREFIX = "sys:dict:";

    private final RedisUtil redisUtil;

    public SysDictDataServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

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
        // 先从缓存获取
        Object cached = redisUtil.get(DICT_CACHE_PREFIX + dictType);
        if (cached != null) {
            return (List<SysDictData>) cached;
        }

        // 缓存没有则从数据库查询
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, 1)
                .orderByAsc(SysDictData::getSort);
        List<SysDictData> dataList = baseMapper.selectList(wrapper);

        // 存入缓存
        if (!dataList.isEmpty()) {
            redisUtil.set(DICT_CACHE_PREFIX + dictType, dataList, 24, TimeUnit.HOURS);
        }

        return dataList;
    }

    @Override
    public void refreshCache(String dictType) {
        if (dictType != null) {
            redisUtil.delete(DICT_CACHE_PREFIX + dictType);
            // 重新加载
            selectDictDataByType(dictType);
        }
    }

    @Override
    public void clearAllCache() {
        redisUtil.deleteByPattern(DICT_CACHE_PREFIX + "*");
    }
}