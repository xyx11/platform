package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysConfig;

/**
 * 参数配置服务接口
 */
public interface SysConfigService extends IServiceX<SysConfig> {

    /**
     * 分页查询参数配置列表
     */
    Page<SysConfig> selectConfigPage(SysConfig config, Integer pageNum, Integer pageSize);

    /**
     * 根据键名查询参数配置
     */
    SysConfig selectConfigByKey(String configKey);

    /**
     * 保存参数配置
     */
    void saveConfig(SysConfig config);

    /**
     * 更新参数配置
     */
    void updateConfig(SysConfig config);
}