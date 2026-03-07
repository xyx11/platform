package com.micro.platform.system.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 服务器信息
 */
public class ServerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * CPU 核心数
     */
    private String cpuProcessors;

    /**
     * JVM 名称
     */
    private String jvmName;

    /**
     * JVM 版本
     */
    private String jvmVersion;

    /**
     * JVM 启动时间
     */
    private String jvmStartTime;

    /**
     * JVM 运行时长
     */
    private String jvmRunTime;

    /**
     * JVM 安装路径
     */
    private String jvmHome;

    /**
     * 最大内存
     */
    private String maxMemory;

    /**
     * 总内存
     */
    private String totalMemory;

    /**
     * 可用内存
     */
    private String freeMemory;

    /**
     * 已使用内存
     */
    private String usableMemory;

    /**
     * 系统名称
     */
    private String osName;

    /**
     * 系统架构
     */
    private String osArch;

    /**
     * 系统版本
     */
    private String osVersion;

    /**
     * 磁盘信息
     */
    private List<DiskInfo> diskInfo;

    public String getCpuProcessors() {
        return cpuProcessors;
    }

    public void setCpuProcessors(String cpuProcessors) {
        this.cpuProcessors = cpuProcessors;
    }

    public String getJvmName() {
        return jvmName;
    }

    public void setJvmName(String jvmName) {
        this.jvmName = jvmName;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public void setJvmVersion(String jvmVersion) {
        this.jvmVersion = jvmVersion;
    }

    public String getJvmStartTime() {
        return jvmStartTime;
    }

    public void setJvmStartTime(String jvmStartTime) {
        this.jvmStartTime = jvmStartTime;
    }

    public String getJvmRunTime() {
        return jvmRunTime;
    }

    public void setJvmRunTime(String jvmRunTime) {
        this.jvmRunTime = jvmRunTime;
    }

    public String getJvmHome() {
        return jvmHome;
    }

    public void setJvmHome(String jvmHome) {
        this.jvmHome = jvmHome;
    }

    public String getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(String maxMemory) {
        this.maxMemory = maxMemory;
    }

    public String getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(String totalMemory) {
        this.totalMemory = totalMemory;
    }

    public String getFreeMemory() {
        return freeMemory;
    }

    public void setFreeMemory(String freeMemory) {
        this.freeMemory = freeMemory;
    }

    public String getUsableMemory() {
        return usableMemory;
    }

    public void setUsableMemory(String usableMemory) {
        this.usableMemory = usableMemory;
    }

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

    public List<DiskInfo> getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(List<DiskInfo> diskInfo) {
        this.diskInfo = diskInfo;
    }

    /**
     * 磁盘信息
     */
    public static class DiskInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String diskName;
        private String total;
        private String used;
        private String free;

        public String getDiskName() {
            return diskName;
        }

        public void setDiskName(String diskName) {
            this.diskName = diskName;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getUsed() {
            return used;
        }

        public void setUsed(String used) {
            this.used = used;
        }

        public String getFree() {
            return free;
        }

        public void setFree(String free) {
            this.free = free;
        }
    }
}