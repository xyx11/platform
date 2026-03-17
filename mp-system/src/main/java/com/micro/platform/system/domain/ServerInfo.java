package com.micro.platform.system.domain;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 服务器信息
 */
@Data
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

    public List<DiskInfo> getDiskInfo() {
        return diskInfo;
    }

    /**
     * 磁盘信息
     */
    @Data
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