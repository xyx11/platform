package com.micro.platform.generator.entity;


import java.io.Serializable;

/**
 * 数据库表列信息
 */
public class TableColumnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 列名
     */
    private String columnName;

    /**
     * 列描述
     */
    private String columnComment;

    /**
     * 列类型
     */
    private String columnType;

    /**
     * Java 字段名
     */
    private String fieldName;

    /**
     * Java 字段类型
     */
    private String fieldType;

    /**
     * 是否为空
     */
    private String isNullable;

    /**
     * 是否主键
     */
    private Boolean isPrimaryKey;

    /**
     * 是否列表字段
     */
    private Boolean isListField;

    /**
     * 是否查询字段
     */
    private Boolean isQueryField;

    /**
     * 查询方式 (EQ:等于 NE:不等于 GT:大于 GE:大于等于 LT:小于 LE:小于等于 LIKE:模糊 BETWEEN:范围)
     */
    private String queryType;

    /**
     * 显示类型 (input:文本框 textarea:文本域 select:下拉框 checkbox:复选框 radio:单选框 datetime:日期控件)
     */
    private String htmlType;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 排序
     */
    private Integer sort;
    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getColumnComment() { return columnComment; }
    public void setColumnComment(String columnComment) { this.columnComment = columnComment; }

    public String getColumnType() { return columnType; }
    public void setColumnType(String columnType) { this.columnType = columnType; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    public String getIsNullable() { return isNullable; }
    public void setIsNullable(String isNullable) { this.isNullable = isNullable; }

    public Boolean getIsPrimaryKey() { return isPrimaryKey; }
    public void setIsPrimaryKey(Boolean isPrimaryKey) { this.isPrimaryKey = isPrimaryKey; }

    public Boolean getIsListField() { return isListField; }
    public void setIsListField(Boolean isListField) { this.isListField = isListField; }

    public Boolean getIsQueryField() { return isQueryField; }
    public void setIsQueryField(Boolean isQueryField) { this.isQueryField = isQueryField; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getHtmlType() { return htmlType; }
    public void setHtmlType(String htmlType) { this.htmlType = htmlType; }

    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }

}
