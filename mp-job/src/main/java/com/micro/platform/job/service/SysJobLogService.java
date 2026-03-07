package com.micro.platform.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.job.entity.SysJobLog;

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
}