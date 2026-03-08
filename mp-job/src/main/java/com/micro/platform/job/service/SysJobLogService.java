package com.micro.platform.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.job.entity.SysJobLog;

import java.util.Map;

/**
 * 定时任务日志服务接口
 */
public interface SysJobLogService extends IServiceX<SysJobLog> {

    /**
     * 分页查询任务日志列表
     */
    Page<SysJobLog> selectJobLogPage(SysJobLog jobLog, Integer pageNum, Integer pageSize);

    /**
     * 清空任务日志
     */
    void clean();

    /**
     * 批量删除任务日志
     */
    void batchRemove(String ids);

    /**
     * 获取任务日志统计信息
     */
    Map<String, Object> getJobLogStats();
}