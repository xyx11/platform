package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.mapper.SysLoginLogMapper;
import com.micro.platform.system.service.SysLoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录日志服务实现
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImplX<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public Page<SysLoginLog> selectLoginLogPage(SysLoginLog log, Integer pageNum, Integer pageSize) {
        Page<SysLoginLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getUsername()), SysLoginLog::getUsername, log.getUsername())
                .eq(log.getStatus() != null, SysLoginLog::getStatus, log.getStatus())
                .eq(log.getUserId() != null, SysLoginLog::getUserId, log.getUserId())
                .like(StringUtils.hasText(log.getIp()), SysLoginLog::getIp, log.getIp())
                .ge(log.getLoginTime() != null, SysLoginLog::getLoginTime, log.getLoginTime())
                .orderByDesc(SysLoginLog::getLoginTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public void clean() {
        baseMapper.clean();
    }
}