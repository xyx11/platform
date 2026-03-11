package com.micro.platform.system.service;

import com.micro.platform.system.entity.ServerInfo;

/**
 * 服务器监控服务
 */
public interface ServerMonitorService {

    /**
     * 获取服务器详细信息
     */
    ServerInfo getServerInfo();

    /**
     * 获取系统运行时间（秒）
     */
    long getSystemUptime();

    /**
     * 获取系统负载（最近 1 分钟）
     */
    double getSystemLoad();

    /**
     * 获取 CPU 使用率
     */
    double getCpuUsage();

    /**
     * 获取内存使用率
     */
    double getMemoryUsage();

    /**
     * 获取磁盘使用率
     * @param path 磁盘路径
     */
    double getDiskUsage(String path);
}