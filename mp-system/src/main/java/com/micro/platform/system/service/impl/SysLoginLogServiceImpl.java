package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysLoginLog;
import com.micro.platform.system.mapper.SysLoginLogMapper;
import com.micro.platform.system.service.SysLoginLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.List;

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

    @Override
    public void exportLoginLog(HttpServletResponse response, SysLoginLog log) {
        try {
            LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(StringUtils.hasText(log.getUsername()), SysLoginLog::getUsername, log.getUsername())
                    .eq(log.getStatus() != null, SysLoginLog::getStatus, log.getStatus())
                    .eq(log.getUserId() != null, SysLoginLog::getUserId, log.getUserId())
                    .like(StringUtils.hasText(log.getIp()), SysLoginLog::getIp, log.getIp())
                    .ge(log.getLoginTime() != null, SysLoginLog::getLoginTime, log.getLoginTime())
                    .orderByDesc(SysLoginLog::getLoginTime);
            List<SysLoginLog> list = list(wrapper);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("登录日志", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysLoginLog.class)
                    .sheet("登录日志")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出登录日志失败：" + e.getMessage());
        }
    }
}