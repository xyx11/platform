package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysConfig;
import com.micro.platform.system.mapper.SysConfigMapper;
import com.micro.platform.system.service.SysConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 参数配置服务实现
 */
@Service
public class SysConfigServiceImpl extends ServiceImplX<SysConfigMapper, SysConfig> implements SysConfigService {

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
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigKey, configKey);
        return baseMapper.selectOne(wrapper);
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
        baseMapper.updateById(config);
    }
}