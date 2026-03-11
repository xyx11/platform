package com.micro.platform.system.entity;

import java.io.Serializable;

/**
 * 服务器信息实体
 */
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

    public CpuInfo getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(CpuInfo cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public MemoryInfo getMemoryInfo() {
        return memoryInfo;
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
        this.memoryInfo = memoryInfo;
    }

    public JvmInfo getJvmInfo() {
        return jvmInfo;
    }

    public void setJvmInfo(JvmInfo jvmInfo) {
        this.jvmInfo = jvmInfo;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public void setSystemInfo(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public DiskInfo getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(DiskInfo diskInfo) {
        this.diskInfo = diskInfo;
    }

    /**
     * CPU 信息
     */
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

        public int getCpuNum() {
            return cpuNum;
        }

        public void setCpuNum(int cpuNum) {
            this.cpuNum = cpuNum;
        }

        public double getCpuUsage() {
            return cpuUsage;
        }

        public void setCpuUsage(double cpuUsage) {
            this.cpuUsage = cpuUsage;
        }

        public String getCpuModel() {
            return cpuModel;
        }

        public void setCpuModel(String cpuModel) {
            this.cpuModel = cpuModel;
        }
    }

    /**
     * 内存信息
     */
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

        public double getTotalMemory() {
            return totalMemory;
        }

        public void setTotalMemory(double totalMemory) {
            this.totalMemory = totalMemory;
        }

        public double getUsedMemory() {
            return usedMemory;
        }

        public void setUsedMemory(double usedMemory) {
            this.usedMemory = usedMemory;
        }

        public double getFreeMemory() {
            return freeMemory;
        }

        public void setFreeMemory(double freeMemory) {
            this.freeMemory = freeMemory;
        }

        public double getMemoryUsage() {
            return memoryUsage;
        }

        public void setMemoryUsage(double memoryUsage) {
            this.memoryUsage = memoryUsage;
        }
    }

    /**
     * JVM 信息
     */
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

        public String getJvmVersion() {
            return jvmVersion;
        }

        public void setJvmVersion(String jvmVersion) {
            this.jvmVersion = jvmVersion;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public long getUptime() {
            return uptime;
        }

        public void setUptime(long uptime) {
            this.uptime = uptime;
        }

        public double getHeapMax() {
            return heapMax;
        }

        public void setHeapMax(double heapMax) {
            this.heapMax = heapMax;
        }

        public double getHeapUsed() {
            return heapUsed;
        }

        public void setHeapUsed(double heapUsed) {
            this.heapUsed = heapUsed;
        }

        public double getNonHeapUsed() {
            return nonHeapUsed;
        }

        public void setNonHeapUsed(double nonHeapUsed) {
            this.nonHeapUsed = nonHeapUsed;
        }

        public int getGcCount() {
            return gcCount;
        }

        public void setGcCount(int gcCount) {
            this.gcCount = gcCount;
        }

        public long getGcTime() {
            return gcTime;
        }

        public void setGcTime(long gcTime) {
            this.gcTime = gcTime;
        }
    }

    /**
     * 系统信息
     */
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

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public String getOsArch() {
            return osArch;
        }

        public void setOsArch(String osArch) {
            this.osArch = osArch;
        }

        public String getOsVersion() {
            return osVersion;
        }

        public void setOsVersion(String osVersion) {
            this.osVersion = osVersion;
        }

        public double getSystemLoad() {
            return systemLoad;
        }

        public void setSystemLoad(double systemLoad) {
            this.systemLoad = systemLoad;
        }

        public int getAvailableProcessors() {
            return availableProcessors;
        }

        public void setAvailableProcessors(int availableProcessors) {
            this.availableProcessors = availableProcessors;
        }
    }

    /**
     * 磁盘信息
     */
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

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public double getTotal() {
            return total;
        }

        public void setTotal(double total) {
            this.total = total;
        }

        public double getUsed() {
            return used;
        }

        public void setUsed(double used) {
            this.used = used;
        }

        public double getFree() {
            return free;
        }

        public void setFree(double free) {
            this.free = free;
        }

        public double getUsage() {
            return usage;
        }

        public void setUsage(double usage) {
            this.usage = usage;
        }
    }
}