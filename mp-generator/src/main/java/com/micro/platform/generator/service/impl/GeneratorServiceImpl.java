package com.micro.platform.generator.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.micro.platform.generator.entity.TableColumnInfo;
import com.micro.platform.generator.entity.TableInfo;
import com.micro.platform.generator.service.GeneratorService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 代码生成服务实现类
 */
@Service
public class GeneratorServiceImpl implements GeneratorService {

    private final JdbcTemplate jdbcTemplate;

    public GeneratorServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TableInfo> listTables(String tableName, Integer pageNum, Integer pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT table_name AS tableName, table_comment AS tableComment, create_time AS createTime ");
        sql.append("FROM information_schema.tables ");
        sql.append("WHERE table_schema = (SELECT DATABASE()) ");
        if (StringUtils.isNotBlank(tableName)) {
            sql.append("AND table_name LIKE ? ");
        }
        sql.append("ORDER BY table_name ");
        sql.append("LIMIT ?, ?");
        
        if (StringUtils.isNotBlank(tableName)) {
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                TableInfo info = new TableInfo();
                info.setTableName(rs.getString("tableName"));
                info.setTableComment(rs.getString("tableComment"));
                info.setCreateTime(rs.getString("createTime"));
                return info;
            }, "%" + tableName + "%", (pageNum - 1) * pageSize, pageSize);
        } else {
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                TableInfo info = new TableInfo();
                info.setTableName(rs.getString("tableName"));
                info.setTableComment(rs.getString("tableComment"));
                info.setCreateTime(rs.getString("createTime"));
                return info;
            }, (pageNum - 1) * pageSize, pageSize);
        }
    }

    @Override
    public List<TableColumnInfo> listColumns(String tableName) {
        String sql = "SELECT column_name AS columnName, column_type AS columnType, column_comment AS columnComment FROM information_schema.columns WHERE table_schema = (SELECT DATABASE()) AND table_name = ? ORDER BY ordinal_position";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TableColumnInfo info = new TableColumnInfo();
            info.setColumnName(rs.getString("columnName"));
            info.setColumnType(rs.getString("columnType"));
            info.setColumnComment(rs.getString("columnComment"));
            return info;
        }, tableName);
    }

    @Override
    public void importTables(List<String> tableNames) {
        // TODO: 实现导入表逻辑
    }

    @Override
    public Map<String, String> previewCode(String tableName) {
        // TODO: 实现预览代码逻辑
        return new HashMap<>();
    }

    @Override
    public byte[] downloadCode(String tableName) {
        // TODO: 实现下载代码逻辑
        return new byte[0];
    }

    @Override
    public void syncDatabase(String tableName) {
        // TODO: 实现同步数据库逻辑
    }

    @Override
    public void deleteTable(Long tableId) {
        // TODO: 实现删除表逻辑
    }
}
