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
        wrapper.like(StringUtils.hasText(log.getModule()), SysOperationLog::getModule, log.getModule())
                .like(StringUtils.hasText(log.getOperationType()), SysOperationLog::getOperationType, log.getOperationType())
                .like(StringUtils.hasText(log.getDescription()), SysOperationLog::getDescription, log.getDescription())
                .like(StringUtils.hasText(log.getOperatorName()), SysOperationLog::getOperatorName, log.getOperatorName())
                .eq(log.getStatus() != null, SysOperationLog::getStatus, log.getStatus())
                .ge(log.getCreateTime() != null, SysOperationLog::getCreateTime, log.getCreateTime())
                .orderByDesc(SysOperationLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}