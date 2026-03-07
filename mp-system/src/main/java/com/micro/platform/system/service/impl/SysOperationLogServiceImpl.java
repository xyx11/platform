package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysOperationLog;
import com.micro.platform.system.mapper.SysOperationLogMapper;
import com.micro.platform.system.service.SysOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImplX<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    @Override
    public Page<SysOperationLog> selectOperationLogPage(SysOperationLog log, Integer pageNum, Integer pageSize) {
        Page<SysOperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getTitle()), SysOperationLog::getTitle, log.getTitle())
                .like(StringUtils.hasText(log.getOperName()), SysOperationLog::getOperName, log.getOperName())
                .eq(log.getBusinessType() != null, SysOperationLog::getBusinessType, log.getBusinessType())
                .eq(log.getStatus() != null, SysOperationLog::getStatus, log.getStatus())
                .ge(log.getOperTime() != null, SysOperationLog::getOperTime, log.getOperTime())
                .orderByDesc(SysOperationLog::getOperTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void clean() {
        baseMapper.clean();
    }
}