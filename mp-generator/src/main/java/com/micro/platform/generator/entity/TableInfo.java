package com.micro.platform.generator.entity;


import java.io.Serializable;

/**
 * 数据库表信息
 */
public class TableInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表描述
     */
    private String tableComment;

    /**
     * 实体类名称
     */
    private String className;

    /**
     * 主键列名
     */
    private String pkColumnName;

    /**
     * 主键字段名
     */
    private String pkFieldName;

    /**
     * 主键类型
     */
    private String pkColumnType;

    /**
     * 创建时间
     */
    private String createTime;
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getTableComment() { return tableComment; }
    public void setTableComment(String tableComment) { this.tableComment = tableComment; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPkColumnName() { return pkColumnName; }
    public void setPkColumnName(String pkColumnName) { this.pkColumnName = pkColumnName; }

    public String getPkFieldName() { return pkFieldName; }
    public void setPkFieldName(String pkFieldName) { this.pkFieldName = pkFieldName; }

    public String getPkColumnType() { return pkColumnType; }
    public void setPkColumnType(String pkColumnType) { this.pkColumnType = pkColumnType; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

}
