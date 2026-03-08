package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysOperationLog;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

/**
 * 操作日志服务接口
 */
public interface SysOperationLogService extends IServiceX<SysOperationLog> {

    /**
     * 分页查询操作日志列表
     */
    Page<SysOperationLog> selectOperationLogPage(SysOperationLog log, Integer pageNum, Integer pageSize);

    /**
     * 清空操作日志
     */
    void clean();

    /**
     * 导出操作日志
     */
    void exportOperationLog(HttpServletResponse response, SysOperationLog log);

    /**
     * 获取操作日志统计信息
     */
    Map<String, Object> getOperationLogStats();
}