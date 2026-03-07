package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysOperationLog;

/**
 * 操作日志服务接口
 */
public interface SysOperationLogService extends IServiceX<SysOperationLog> {

    /**
     * 分页查询操作日志列表
     */
    Page<SysOperationLog> selectOperationLogPage(SysOperationLog log, Integer pageNum, Integer pageSize);
}