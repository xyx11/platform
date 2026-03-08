package com.micro.platform.job.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.job.entity.SysJob;
import com.micro.platform.job.entity.SysJobLog;
import com.micro.platform.job.mapper.SysJobLogMapper;
import com.micro.platform.job.mapper.SysJobMapper;
import com.micro.platform.job.service.SysJobService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务服务实现
 */
@Service
public class SysJobServiceImpl extends ServiceImplX<SysJobMapper, SysJob> implements SysJobService {

    private final SysJobLogMapper sysJobLogMapper;

    public SysJobServiceImpl(SysJobLogMapper sysJobLogMapper) {
        this.sysJobLogMapper = sysJobLogMapper;
    }

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
        SysJob job = getById(jobId);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        // 创建执行日志
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobId(job.getJobId());
        jobLog.setJobName(job.getJobName());
        jobLog.setJobGroup(job.getJobGroup());
        jobLog.setInvokeTarget(job.getInvokeTarget());
        jobLog.setJobMessage("任务执行成功");
        jobLog.setStatus(0);
        sysJobLogMapper.insert(jobLog);
    }

    @Override
    public void start(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        job.setStatus(1); // 1:正常
        updateById(job);
    }

    @Override
    public void stop(Long jobId) {
        SysJob job = getById(jobId);
        if (job == null) {
            throw new RuntimeException("任务不存在");
        }
        job.setStatus(0); // 0:暂停
        updateById(job);
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

    @Override
    public Map<String, Object> getJobStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计任务总数
        long totalJobCount = baseMapper.selectCount(null);
        stats.put("totalJobCount", totalJobCount);

        // 统计正常运行任务数
        long runningJobCount = baseMapper.selectCount(new LambdaQueryWrapper<SysJob>().eq(SysJob::getStatus, 1));
        stats.put("runningJobCount", runningJobCount);

        // 统计暂停任务数
        long stoppedJobCount = baseMapper.selectCount(new LambdaQueryWrapper<SysJob>().eq(SysJob::getStatus, 0));
        stats.put("stoppedJobCount", stoppedJobCount);

        // 统计今日执行次数
        LambdaQueryWrapper<SysJobLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysJobLog::getCreateTime, java.time.LocalDate.now().atStartOfDay());
        long todayExecuteCount = sysJobLogMapper.selectCount(todayWrapper);
        stats.put("todayExecuteCount", todayExecuteCount);

        // 统计今日成功次数
        LambdaQueryWrapper<SysJobLog> todaySuccessWrapper = new LambdaQueryWrapper<>();
        todaySuccessWrapper.ge(SysJobLog::getCreateTime, java.time.LocalDate.now().atStartOfDay())
                .eq(SysJobLog::getStatus, 0);
        long todaySuccessCount = sysJobLogMapper.selectCount(todaySuccessWrapper);
        stats.put("todaySuccessCount", todaySuccessCount);

        // 统计今日失败次数
        LambdaQueryWrapper<SysJobLog> todayFailWrapper = new LambdaQueryWrapper<>();
        todayFailWrapper.ge(SysJobLog::getCreateTime, java.time.LocalDate.now().atStartOfDay())
                .eq(SysJobLog::getStatus, 1);
        long todayFailCount = sysJobLogMapper.selectCount(todayFailWrapper);
        stats.put("todayFailCount", todayFailCount);

        // 统计总执行次数
        long totalExecuteCount = sysJobLogMapper.selectCount(null);
        stats.put("totalExecuteCount", totalExecuteCount);

        // 计算成功率
        double successRate = totalExecuteCount > 0 ? (double) todaySuccessCount / totalExecuteCount * 100 : 0;
        stats.put("successRate", String.format("%.2f", successRate));

        return stats;
    }
}