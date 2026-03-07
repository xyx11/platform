package com.micro.platform.job.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.job.entity.SysJob;
import com.micro.platform.job.mapper.SysJobMapper;
import com.micro.platform.job.service.SysJobService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.List;

/**
 * 定时任务服务实现
 */
@Service
public class SysJobServiceImpl extends ServiceImplX<SysJobMapper, SysJob> implements SysJobService {

    @Override
    public Page<SysJob> selectJobPage(SysJob job, Integer pageNum, Integer pageSize) {
        Page<SysJob> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(job.getJobName()), SysJob::getJobName, job.getJobName())
                .eq(StringUtils.hasText(job.getJobGroup()), SysJob::getJobGroup, job.getJobGroup())
                .eq(job.getStatus() != null, SysJob::getStatus, job.getStatus())
                .orderByDesc(SysJob::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void run(Long jobId) {
        // TODO: 调用 XXL-Job API 立即执行一次任务
        SysJob job = getById(jobId);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        // 这里需要调用 XXL-Job 的 API 来触发执行
        // 由于 XXL-Job 主要通过调度中心管理，这里仅做记录
    }

    @Override
    public void start(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        job.setStatus(1); // 1:正常
        updateById(job);
        // TODO: 调用 XXL-Job API 添加/更新调度任务
    }

    @Override
    public void stop(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        job.setStatus(0); // 0:暂停
        updateById(job);
        // TODO: 调用 XXL-Job API 移除调度任务
    }

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        LambdaQueryWrapper<SysJob> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(job.getJobName()), SysJob::getJobName, job.getJobName())
                .eq(StringUtils.hasText(job.getJobGroup()), SysJob::getJobGroup, job.getJobGroup())
                .eq(job.getStatus() != null, SysJob::getStatus, job.getStatus())
                .orderByDesc(SysJob::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.removeByIds(ids);
        }
    }

    @Override
    public void exportJob(HttpServletResponse response, SysJob job) {
        try {
            List<SysJob> list = selectJobList(job);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("定时任务", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysJob.class)
                    .sheet("定时任务")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出定时任务失败：" + e.getMessage());
        }
    }
}