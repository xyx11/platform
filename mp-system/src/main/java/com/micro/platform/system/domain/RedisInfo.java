package com.micro.platform.system.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Redis 信息
 */
public class RedisInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Redis 版本
     */
    private String version;

    /**
     * 运行天数
     */
    private String uptime;

    /**
     * 连接客户端数量
     */
    private String connectedClients;

    /**
     * 内存使用
     */
    private String usedMemory;

    /**
     * 内存使用峰值
     */
    private String usedMemoryPeak;

    /**
     * 内存使用率
     */
    private String usedMemoryRatio;

    /**
     * 内存碎片比率
     */
    private String memFragmentationRatio;

    /**
     * 执行的命令数
     */
    private String totalCommandsProcessed;

    /**
     * 每秒操作数
     */
    private String instantaneousOpsPerSec;

    /**
     * 缓存命中次数
     */
    private String keyspaceHits;

    /**
     * 缓存miss 次数
     */
    private String keyspaceMisses;

    /**
     * 数据库信息
     */
    private List<DbInfo> dbInfo;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(String connectedClients) {
        this.connectedClients = connectedClients;
    }

    public String getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(String usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getUsedMemoryPeak() {
        return usedMemoryPeak;
    }

    public void setUsedMemoryPeak(String usedMemoryPeak) {
        this.usedMemoryPeak = usedMemoryPeak;
    }

    public String getUsedMemoryRatio() {
        return usedMemoryRatio;
    }

    public void setUsedMemoryRatio(String usedMemoryRatio) {
        this.usedMemoryRatio = usedMemoryRatio;
    }

    public String getMemFragmentationRatio() {
        return memFragmentationRatio;
    }

    public void setMemFragmentationRatio(String memFragmentationRatio) {
        this.memFragmentationRatio = memFragmentationRatio;
    }

    public String getTotalCommandsProcessed() {
        return totalCommandsProcessed;
    }

    public void setTotalCommandsProcessed(String totalCommandsProcessed) {
        this.totalCommandsProcessed = totalCommandsProcessed;
    }

    public String getInstantaneousOpsPerSec() {
        return instantaneousOpsPerSec;
    }

    public void setInstantaneousOpsPerSec(String instantaneousOpsPerSec) {
        this.instantaneousOpsPerSec = instantaneousOpsPerSec;
    }

    public String getKeyspaceHits() {
        return keyspaceHits;
    }

    public void setKeyspaceHits(String keyspaceHits) {
        this.keyspaceHits = keyspaceHits;
    }

    public String getKeyspaceMisses() {
        return keyspaceMisses;
    }

    public void setKeyspaceMisses(String keyspaceMisses) {
        this.keyspaceMisses = keyspaceMisses;
    }

    public List<DbInfo> getDbInfo() {
        return dbInfo;
    }

    public void setDbInfo(List<DbInfo> dbInfo) {
        this.dbInfo = dbInfo;
    }

    /**
     * 数据库信息
     */
    public static class DbInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String dbName;
        private Integer keysCount;

        public String getDbName() {
            return dbName;
        }

        public void setDbName(String dbName) {
            this.dbName = dbName;
        }

        public Integer getKeysCount() {
            return keysCount;
        }

        public void setKeysCount(Integer keysCount) {
            this.keysCount = keysCount;
        }
    }
}