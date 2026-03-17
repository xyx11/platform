package com.micro.platform.system.domain;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * Redis 信息
 */
@Data
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

    public List<DbInfo> getDbInfo() {
        return dbInfo;
    }

    /**
     * 数据库信息
     */
    @Data
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