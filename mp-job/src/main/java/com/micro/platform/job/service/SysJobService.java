package com.micro.platform.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.job.entity.SysJob;
import com.micro.platform.job.entity.SysJobLog;

/**
 * 定时任务服务接口
 */
public interface SysJobService extends IServiceX<SysJob> {

    /**
     * 分页查询任务列表
     */
    Page<SysJob> selectJobPage(SysJob job, Integer pageNum, Integer pageSize);

    /**
     * 立即执行一次任务
     */
    void run(Long jobId);

    /**
     * 启动任务
     */
    void start(Long jobId);

    /**
     * 停止任务
     */
    void stop(Long jobId);
}