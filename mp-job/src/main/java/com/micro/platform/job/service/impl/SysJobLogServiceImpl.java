package com.micro.platform.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.job.entity.SysJobLog;
import com.micro.platform.job.mapper.SysJobLogMapper;
import com.micro.platform.job.service.SysJobLogService;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * 定时任务日志服务实现
 */
@Service
public class SysJobLogServiceImpl extends ServiceImplX<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    @Override
    public Page<SysJobLog> selectJobLogPage(SysJobLog jobLog, Integer pageNum, Integer pageSize) {
        Page<SysJobLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysJobLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(jobLog.getJobId() != null, SysJobLog::getJobId, jobLog.getJobId())
                .like(StringUtils.hasText(jobLog.getJobName()), SysJobLog::getJobName, jobLog.getJobName())
                .eq(StringUtils.hasText(jobLog.getJobGroup()), SysJobLog::getJobGroup, jobLog.getJobGroup())
                .eq(jobLog.getStatus() != null, SysJobLog::getStatus, jobLog.getStatus())
                .orderByDesc(SysJobLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void clean() {
        baseMapper.clean();
    }

    @Override
    public void batchRemove(String ids) {
        if (StringUtils.hasText(ids)) {
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            removeByIds(idList);
        }
    }
}
