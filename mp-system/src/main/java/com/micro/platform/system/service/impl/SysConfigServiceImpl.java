package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.common.redis.util.RedisUtil;
import com.micro.platform.system.entity.SysConfig;
import com.micro.platform.system.mapper.SysConfigMapper;
import com.micro.platform.system.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 参数配置服务实现
 */
@Service
public class SysConfigServiceImpl extends ServiceImplX<SysConfigMapper, SysConfig> implements SysConfigService {

    private static final String CONFIG_KEY_PREFIX = "sys:config:";

    private final RedisUtil redisUtil;

    public SysConfigServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public Page<SysConfig> selectConfigPage(SysConfig config, Integer pageNum, Integer pageSize) {
        Page<SysConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
                .like(StringUtils.hasText(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
                .eq(config.getStatus() != null, SysConfig::getStatus, config.getStatus())
                .orderByDesc(SysConfig::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public SysConfig selectConfigByKey(String configKey) {
        // 先从缓存中获取
        String cacheKey = CONFIG_KEY_PREFIX + configKey;
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return (SysConfig) cached;
        }

        // 缓存没有则从数据库查询
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        SysConfig config = baseMapper.selectOne(wrapper);

        // 存入缓存
        if (config != null) {
            redisUtil.set(cacheKey, config, 30, TimeUnit.MINUTES);
        }
        return config;
    }

    @Override
    public String selectConfigValueByKey(String configKey) {
        SysConfig config = selectConfigByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void saveConfig(SysConfig config) {
        SysConfig existConfig = selectConfigByKey(config.getConfigKey());
        if (existConfig != null) {
            throw new BusinessException("参数键名已存在");
        }
        baseMapper.insert(config);
    }

    @Override
    public void updateConfig(SysConfig config) {
        // 更新数据库
        baseMapper.updateById(config);

        // 删除缓存
        String cacheKey = CONFIG_KEY_PREFIX + config.getConfigKey();
        redisUtil.delete(cacheKey);
    }

    @Override
    public void refreshCache() {
        // 查询所有参数配置
        List<SysConfig> configs = list();

        // 清空旧的缓存
        redisUtil.deleteByPattern(CONFIG_KEY_PREFIX + "*");

        // 重新加载所有配置到缓存
        for (SysConfig config : configs) {
            if (config.getStatus() == 1) { // 只缓存正常的配置
                String cacheKey = CONFIG_KEY_PREFIX + config.getConfigKey();
                redisUtil.set(cacheKey, config, 30, TimeUnit.MINUTES);
            }
        }
    }

    @Override
    public Map<String, Object> getConfigStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计参数配置总数
        long totalConfigCount = baseMapper.selectCount(null);
        stats.put("totalConfigCount", totalConfigCount);

        // 统计正常状态的配置
        long activeConfigCount = baseMapper.selectCount(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getStatus, 1));
        stats.put("activeConfigCount", activeConfigCount);

        // 统计禁用状态的配置
        long disabledConfigCount = baseMapper.selectCount(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getStatus, 0));
        stats.put("disabledConfigCount", disabledConfigCount);

        // 内置配置数量（configType 为 1）
        long builtInConfigCount = baseMapper.selectCount(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigType, 1));
        stats.put("builtInConfigCount", builtInConfigCount);

        // 自定义配置数量（configType 为 2）
        long customConfigCount = baseMapper.selectCount(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigType, 2));
        stats.put("customConfigCount", customConfigCount);

        return stats;
    }

    @Override
    public void batchSaveConfig(List<SysConfig> configs) {
        for (SysConfig config : configs) {
            try {
                SysConfig existConfig = selectConfigByKey(config.getConfigKey());
                if (existConfig != null) {
                    // 已存在则更新
                    config.setConfigId(existConfig.getConfigId());
                    updateConfig(config);
                } else {
                    // 不存在则新增
                    saveConfig(config);
                }
            } catch (Exception e) {
                throw new BusinessException("批量保存配置失败：" + e.getMessage());
            }
        }
    }

    @Override
    public byte[] exportConfig(SysConfig config) {
        try {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
                    .like(StringUtils.hasText(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
                    .eq(config.getStatus() != null, SysConfig::getStatus, config.getStatus())
                    .orderByDesc(SysConfig::getCreateTime);
            List<SysConfig> list = baseMapper.selectList(wrapper);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            EasyExcel.write(os, SysConfig.class)
                    .sheet("参数配置")
                    .doWrite(list);
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出参数配置失败：" + e.getMessage());
        }
    }
}