package com.micro.platform.system.service.impl;

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

import java.util.List;
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

        // 缓存中没有则从数据库查询
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
}