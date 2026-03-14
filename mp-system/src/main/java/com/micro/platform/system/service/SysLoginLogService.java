package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysLoginLog;

import java.util.Map;

/**
 * 登录日志服务接口
 */
public interface SysLoginLogService extends IServiceX<SysLoginLog> {

    /**
     * 分页查询登录日志列表
     */
    Page<SysLoginLog> selectLoginLogPage(SysLoginLog log, Integer pageNum, Integer pageSize);

    /**
     * 清空登录日志
     */
    void clean();

    /**
     * 导出登录日志
     */
    byte[] exportLoginLog(SysLoginLog log);

    /**
     * 获取登录日志统计信息
     */
    Map<String, Object> getLoginLogStats();
}