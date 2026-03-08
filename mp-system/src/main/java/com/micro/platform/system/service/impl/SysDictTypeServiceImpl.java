package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.redis.util.RedisUtil;
import com.micro.platform.system.entity.SysDictData;
import com.micro.platform.system.entity.SysDictType;
import com.micro.platform.system.mapper.SysDictDataMapper;
import com.micro.platform.system.mapper.SysDictTypeMapper;
import com.micro.platform.system.service.SysDictDataService;
import com.micro.platform.system.service.SysDictTypeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 字典类型服务实现
 */
@Service
public class SysDictTypeServiceImpl extends ServiceImplX<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private static final String DICT_CACHE_PREFIX = "sys:dict:";

    private final SysDictDataMapper sysDictDataMapper;
    private final RedisUtil redisUtil;

    public SysDictTypeServiceImpl(SysDictDataMapper sysDictDataMapper, RedisUtil redisUtil) {
        this.sysDictDataMapper = sysDictDataMapper;
        this.redisUtil = redisUtil;
    }

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

    @Override
    public void exportDictType(HttpServletResponse response, SysDictType dictType) {
        try {
            List<SysDictType> list = selectDictTypeList(dictType);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("字典类型数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysDictType.class)
                    .sheet("字典类型")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出字典类型数据失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshCache() {
        // 清空旧的字典缓存
        redisUtil.deleteByPattern(DICT_CACHE_PREFIX + "*");

        // 重新加载所有字典数据
        LambdaQueryWrapper<SysDictType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.eq(SysDictType::getStatus, 1);
        List<SysDictType> types = baseMapper.selectList(typeWrapper);

        for (SysDictType type : types) {
            LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.eq(SysDictData::getDictType, type.getDictType())
                    .eq(SysDictData::getStatus, 1)
                    .orderByAsc(SysDictData::getSort);
            List<SysDictData> dataList = sysDictDataMapper.selectList(dataWrapper);

            redisUtil.set(DICT_CACHE_PREFIX + type.getDictType(), dataList, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void clearCache(String dictType) {
        if (dictType != null) {
            redisUtil.delete(DICT_CACHE_PREFIX + dictType);
        }
    }

    @Override
    public List<Map<String, Object>> getDictTree() {
        List<Map<String, Object>> tree = new ArrayList<>();

        // 查询所有字典类型
        LambdaQueryWrapper<SysDictType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.eq(SysDictType::getStatus, 1).orderByAsc(SysDictType::getCreateTime);
        List<SysDictType> types = baseMapper.selectList(typeWrapper);

        for (SysDictType type : types) {
            Map<String, Object> typeMap = new LinkedHashMap<>();
            typeMap.put("dictId", type.getDictId());
            typeMap.put("dictName", type.getDictName());
            typeMap.put("dictType", type.getDictType());
            typeMap.put("isExpand", false);

            // 查询该类型下的字典数据
            LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.eq(SysDictData::getDictType, type.getDictType())
                    .eq(SysDictData::getStatus, 1)
                    .orderByAsc(SysDictData::getSort);
            List<SysDictData> dataList = sysDictDataMapper.selectList(dataWrapper);

            List<Map<String, Object>> children = new ArrayList<>();
            for (SysDictData data : dataList) {
                Map<String, Object> dataMap = new LinkedHashMap<>();
                dataMap.put("dictCode", data.getDictCode());
                dataMap.put("dictLabel", data.getDictLabel());
                dataMap.put("dictValue", data.getDictValue());
                dataMap.put("sort", data.getSort());
                dataMap.put("isDefault", data.getIsDefault());
                children.add(dataMap);
            }

            typeMap.put("children", children);
            tree.add(typeMap);
        }

        return tree;
    }

    @Override
    public Map<String, Long> getDictStats() {
        Map<String, Long> stats = new HashMap<>();

        // 统计字典类型数量
        long typeCount = baseMapper.selectCount(null);
        stats.put("typeCount", typeCount);

        // 统计字典数据数量
        long dataCount = sysDictDataMapper.selectCount(null);
        stats.put("dataCount", dataCount);

        // 统计每个类型的数据数量
        LambdaQueryWrapper<SysDictType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.select(SysDictType::getDictId, SysDictType::getDictType);
        List<SysDictType> types = baseMapper.selectList(typeWrapper);

        for (SysDictType type : types) {
            LambdaQueryWrapper<SysDictData> dataWrapper = new LambdaQueryWrapper<>();
            dataWrapper.eq(SysDictData::getDictType, type.getDictType());
            long count = sysDictDataMapper.selectCount(dataWrapper);
            stats.put("dataCount_" + type.getDictType(), count);
        }

        return stats;
    }
}