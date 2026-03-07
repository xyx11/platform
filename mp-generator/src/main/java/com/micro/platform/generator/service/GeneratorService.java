package com.micro.platform.generator.service;

import com.micro.platform.generator.entity.TableInfo;
import com.micro.platform.generator.entity.TableColumnInfo;

import java.util.List;
import java.util.Map;

/**
 * 代码生成服务接口
 */
public interface GeneratorService {

    /**
     * 查询数据库表列表
     */
    List<TableInfo> listTables(String tableName, Integer pageNum, Integer pageSize);

    /**
     * 获取表的列信息
     */
    List<TableColumnInfo> listColumns(String tableName);

    /**
     * 导入表
     */
    void importTables(List<String> tableNames);

    /**
     * 预览代码
     */
    Map<String, String> previewCode(String tableName);

    /**
     * 生成代码并下载到本地
     */
    byte[] downloadCode(String tableName);

    /**
     * 同步数据库
     */
    void syncDatabase(String tableName);

    /**
     * 删除表
     */
    void deleteTable(Long tableId);
}
