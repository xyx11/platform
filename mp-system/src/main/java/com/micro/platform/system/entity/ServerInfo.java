package com.micro.platform.system.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

/**
 * 服务器信息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * CPU 信息
     */
    private CpuInfo cpuInfo;

    /**
     * 内存信息
     */
    private MemoryInfo memoryInfo;

    /**
     * JVM 信息
     */
    private JvmInfo jvmInfo;

    /**
     * 系统信息
     */
    private SystemInfo systemInfo;

    /**
     * 磁盘信息
     */
    private DiskInfo diskInfo;

    /**
     * CPU 信息
     */
    @Data
    public static class CpuInfo {
        /**
         * CPU 核心数
         */
        private int cpuNum;

        /**
         * CPU 使用率
         */
        private double cpuUsage;

        /**
         * CPU 型号
         */
        private String cpuModel;
    }

    /**
     * 内存信息
     */
    @Data
    public static class MemoryInfo {
        /**
         * 总内存 (MB)
         */
        private double totalMemory;

        /**
         * 已使用内存 (MB)
         */
        private double usedMemory;

        /**
         * 空闲内存 (MB)
         */
        private double freeMemory;

        /**
         * 内存使用率
         */
        private double memoryUsage;
    }

    /**
     * JVM 信息
     */
    @Data
    public static class JvmInfo {
        /**
         * JVM 版本
         */
        private String jvmVersion;

        /**
         * JVM 启动时间
         */
        private String startTime;

        /**
         * 运行时长 (毫秒)
         */
        private long uptime;

        /**
         * 堆内存最大值 (MB)
         */
        private double heapMax;

        /**
         * 堆内存已使用 (MB)
         */
        private double heapUsed;

        /**
         * 非堆内存已使用 (MB)
         */
        private double nonHeapUsed;

        /**
         * GC 次数
         */
        private int gcCount;

        /**
         * GC 总时间 (毫秒)
         */
        private long gcTime;
    }

    /**
     * 系统信息
     */
    @Data
    public static class SystemInfo {
        /**
         * 操作系统名称
         */
        private String osName;

        /**
         * 操作系统架构
         */
        private String osArch;

        /**
         * 操作系统版本
         */
        private String osVersion;

        /**
         * 系统负载
         */
        private double systemLoad;

        /**
         * 可用进程数
         */
        private int availableProcessors;
    }

    /**
     * 磁盘信息
     */
    @Data
    public static class DiskInfo {
        /**
         * 盘符路径
         */
        private String path;

        /**
         * 总大小 (GB)
         */
        private double total;

        /**
         * 已使用大小 (GB)
         */
        private double used;

        /**
         * 剩余大小 (GB)
         */
        private double free;

        /**
         * 已使用百分比
         */
        private double usage;
    }
}