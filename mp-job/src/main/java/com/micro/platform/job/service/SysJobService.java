package com.micro.platform.job.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.job.entity.SysJob;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

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

    /**
     * 批量删除任务
     */
    void batchDelete(List<Long> ids);

    /**
     * 导出任务数据
     */
    void exportJob(HttpServletResponse response, SysJob job);

    /**
     * 查询任务列表（支持条件查询）
     */
    List<SysJob> selectJobList(SysJob job);
}