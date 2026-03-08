package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysCommandLog;

import java.util.Map;

/**
 * 命令执行记录服务接口
 */
public interface SysCommandLogService extends IServiceX<SysCommandLog> {

    /**
     * 分页查询命令执行记录
     */
    Page<SysCommandLog> selectCommandLogPage(SysCommandLog log, Integer pageNum, Integer pageSize);

    /**
     * 执行命令
     */
    String executeCommand(String commandType, String command);

    /**
     * 清空记录
     */
    void clean();

    /**
     * 获取命令执行统计信息
     */
    Map<String, Object> getCommandLogStats();
}