package com.micro.platform.system.service;

import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysLoginLog;

import java.util.Map;

/**
 * 首页统计服务接口
 */
public interface SysIndexService extends IServiceX<SysLoginLog> {

    /**
     * 获取统计数据
     */
    Map<String, Object> getStatistics();

    /**
     * 获取访问趋势
     */
    Map<String, Object> getVisitTrend();

    /**
     * 获取用户分布
     */
    Map<String, Object> getUserDistribution();
}