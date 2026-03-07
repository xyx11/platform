package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysOperationLog;
import com.micro.platform.system.mapper.SysOperationLogMapper;
import com.micro.platform.system.service.SysOperationLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.List;

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

    @Override
    public void exportOperationLog(HttpServletResponse response, SysOperationLog log) {
        try {
            List<SysOperationLog> list = selectOperationLogList(log);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("操作日志", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysOperationLog.class)
                    .sheet("操作日志")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出操作日志失败：" + e.getMessage());
        }
    }

    /**
     * 查询操作日志列表
     */
    private List<SysOperationLog> selectOperationLogList(SysOperationLog log) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(log.getTitle()), SysOperationLog::getTitle, log.getTitle())
                .like(StringUtils.hasText(log.getOperName()), SysOperationLog::getOperName, log.getOperName())
                .eq(log.getBusinessType() != null, SysOperationLog::getBusinessType, log.getBusinessType())
                .eq(log.getStatus() != null, SysOperationLog::getStatus, log.getStatus())
                .ge(log.getOperTime() != null, SysOperationLog::getOperTime, log.getOperTime())
                .orderByDesc(SysOperationLog::getOperTime);
        return baseMapper.selectList(wrapper);
    }
}