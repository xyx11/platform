package com.micro.platform.generator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.micro.platform.generator.entity.TableColumnInfo;
import com.micro.platform.generator.entity.TableInfo;
import com.micro.platform.generator.service.GeneratorService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
                // 设置类名
                info.setClassName(tableToClassName(rs.getString("tableName")));
                // 查询主键
                String pkInfo = queryPrimaryKey(rs.getString("tableName"));
                if (pkInfo != null) {
                    String[] parts = pkInfo.split(":");
                    info.setPkColumnName(parts[0]);
                    info.setPkFieldName(columnToField(parts[0]));
                    info.setPkColumnType(parts[1]);
                }
                return info;
            }, "%" + tableName + "%", (pageNum - 1) * pageSize, pageSize);
        } else {
            return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                TableInfo info = new TableInfo();
                info.setTableName(rs.getString("tableName"));
                info.setTableComment(rs.getString("tableComment"));
                info.setCreateTime(rs.getString("createTime"));
                info.setClassName(tableToClassName(rs.getString("tableName")));
                String pkInfo = queryPrimaryKey(rs.getString("tableName"));
                if (pkInfo != null) {
                    String[] parts = pkInfo.split(":");
                    info.setPkColumnName(parts[0]);
                    info.setPkFieldName(columnToField(parts[0]));
                    info.setPkColumnType(parts[1]);
                }
                return info;
            }, (pageNum - 1) * pageSize, pageSize);
        }
    }

    @Override
    public List<TableColumnInfo> listColumns(String tableName) {
        String sql = "SELECT column_name AS columnName, column_type AS columnType, column_comment AS columnComment, is_nullable AS isNullable, column_key AS columnKey FROM information_schema.columns WHERE table_schema = (SELECT DATABASE()) AND table_name = ? ORDER BY ordinal_position";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TableColumnInfo info = new TableColumnInfo();
            String columnName = rs.getString("columnName");
            String columnType = rs.getString("columnType");
            String columnComment = rs.getString("columnComment");
            String isNullable = rs.getString("isNullable");
            String columnKey = rs.getString("columnKey");

            info.setColumnName(columnName);
            info.setColumnType(columnType);
            info.setColumnComment(columnComment);
            info.setIsNullable(isNullable);
            info.setIsPrimaryKey("PRI".equals(columnKey));
            info.setFieldName(columnToField(columnName));
            info.setFieldType(columnToFieldType(columnType, isNullable));

            // 默认都是列表字段和查询字段
            info.setIsListField(!info.getIsPrimaryKey());
            info.setIsQueryField(!info.getIsPrimaryKey());
            info.setQueryType("EQ");
            info.setHtmlType("input");
            info.setSort(rowNum);

            return info;
        }, tableName);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importTables(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            return;
        }
        // 这里可以保存表信息到数据库，用于后续代码生成
        // 当前实现直接从 information_schema 读取，所以导入主要是为了记录
        for (String tableName : tableNames) {
            // 可以添加 gen_table 和 gen_table_column 表的插入逻辑
        }
    }

    @Override
    public Map<String, String> previewCode(String tableName) {
        Map<String, String> codeMap = new LinkedHashMap<>();

        // 获取表信息
        List<TableInfo> tables = listTables(tableName, 1, 1);
        if (tables.isEmpty()) {
            return codeMap;
        }
        TableInfo tableInfo = tables.get(0);

        // 获取列信息
        List<TableColumnInfo> columns = listColumns(tableName);

        // 生成各种代码
        codeMap.put("Entity.java", generateEntity(tableInfo, columns));
        codeMap.put("Mapper.java", generateMapper(tableInfo, columns));
        codeMap.put("Mapper.xml", generateMapperXml(tableInfo, columns));
        codeMap.put("Service.java", generateService(tableInfo, columns));
        codeMap.put("ServiceImpl.java", generateServiceImpl(tableInfo, columns));
        codeMap.put("Controller.java", generateController(tableInfo, columns));

        return codeMap;
    }

    @Override
    public byte[] downloadCode(String tableName) throws IOException {
        Map<String, String> codeMap = previewCode(tableName);
        if (codeMap.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        TableInfo tableInfo = listTables(tableName, 1, 1).get(0);
        String basePackage = "com.micro.platform.system";
        String entityPath = basePackage.replace(".", "/") + "/entity/";
        String mapperPath = basePackage.replace(".", "/") + "/mapper/";
        String servicePath = basePackage.replace(".", "/") + "/service/";
        String serviceImplPath = basePackage.replace(".", "/") + "/service/impl/";
        String controllerPath = basePackage.replace(".", "/") + "/controller/";

        // Entity
        addZipEntry(zos, entityPath + tableInfo.getClassName() + ".java", codeMap.get("Entity.java"));
        // Mapper
        addZipEntry(zos, mapperPath + tableInfo.getClassName().replace("Sys", "") + "Mapper.java", codeMap.get("Mapper.java"));
        addZipEntry(zos, mapperPath + tableInfo.getClassName().replace("Sys", "") + "Mapper.xml", codeMap.get("Mapper.xml"));
        // Service
        addZipEntry(zos, servicePath + tableInfo.getClassName().replace("Sys", "") + "Service.java", codeMap.get("Service.java"));
        addZipEntry(zos, serviceImplPath + tableInfo.getClassName().replace("Sys", "") + "ServiceImpl.java", codeMap.get("ServiceImpl.java"));
        // Controller
        addZipEntry(zos, controllerPath + tableInfo.getClassName().replace("Sys", "") + "Controller.java", codeMap.get("Controller.java"));

        zos.close();
        return baos.toByteArray();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDatabase(String tableName) {
        // 同步数据库结构变化
        // 可以从 information_schema 重新读取表结构并更新到 gen_table 相关表
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTable(Long tableId) {
        // 删除生成的表记录
        // 从 gen_table 和 gen_table_column 删除
    }

    // ==================== 辅助方法 ====================

    /**
     * 查询主键信息
     */
    private String queryPrimaryKey(String tableName) {
        String sql = "SELECT c.column_name, c.column_type FROM information_schema.key_column_usage k " +
                     "JOIN information_schema.columns c ON k.table_name = c.table_name AND k.column_name = c.column_name " +
                     "WHERE k.table_schema = (SELECT DATABASE()) AND k.table_name = ? AND k.constraint_name = 'PRIMARY'";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, tableName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 表名转类名
     */
    private String tableToClassName(String tableName) {
        // 去掉 sys_ 前缀
        String name = tableName;
        if (name.startsWith("sys_")) {
            name = name.substring(4);
        } else if (name.startsWith("gen_")) {
            name = name.substring(4);
        }
        // 下划线转驼峰
        String[] parts = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    /**
     * 列名转字段名
     */
    private String columnToField(String columnName) {
        String[] parts = columnName.split("_");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
              .append(parts[i].substring(1).toLowerCase());
        }
        return sb.toString();
    }

    /**
     * 数据库类型转 Java 类型
     */
    private String columnToFieldType(String columnType, String isNullable) {
        String type = columnType.toLowerCase();
        String javaType = "String";

        if (type.contains("int") || type.contains("bigint")) {
            javaType = isNullable.equals("YES") ? "Integer" : "int";
            if (type.contains("bigint")) {
                javaType = isNullable.equals("YES") ? "Long" : "long";
            }
        } else if (type.contains("decimal") || type.contains("numeric")) {
            javaType = isNullable.equals("YES") ? "BigDecimal" : "BigDecimal";
        } else if (type.contains("float") || type.contains("double")) {
            javaType = isNullable.equals("YES") ? "Double" : "double";
        } else if (type.contains("datetime") || type.contains("timestamp") || type.contains("date")) {
            javaType = "Date";
        } else if (type.contains("bit") || type.contains("tinyint")) {
            javaType = isNullable.equals("YES") ? "Boolean" : "boolean";
        }

        return javaType;
    }

    /**
     * 添加 ZIP 条目
     */
    private void addZipEntry(ZipOutputStream zos, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    // ==================== 代码生成方法 ====================

    /**
     * 生成 Entity 类
     */
    private String generateEntity(TableInfo table, List<TableColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        String className = table.getClassName();
        String packageName = "com.micro.platform.system.entity";

        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.annotation.*;\n");
        sb.append("import io.swagger.v3.oas.annotations.tags.Tag;\n");
        sb.append("import io.swagger.v3.oas.annotations.Parameter;\n");
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.util.Date;\n\n");
        sb.append("@Tag(name = \"\"").append(table.getTableComment()).append("\"\")\n");
        sb.append("@TableName(\"").append(table.getTableName()).append("\")\n");
        sb.append("public class ").append(className).append(" implements Serializable {\n\n");
        sb.append("    private static final long serialVersionUID = 1L;\n\n");

        for (TableColumnInfo col : columns) {
            if (col.getIsPrimaryKey()) {
                sb.append("    @TableId(value = \"").append(col.getColumnName()).append("\", type = IdType.AUTO)\n");
            } else if ("deleted".equals(col.getFieldName())) {
                sb.append("    @TableLogic\n");
                sb.append("    @TableField(\"").append(col.getColumnName()).append("\")\n");
            } else if ("create_time".equals(col.getFieldName()) || "update_time".equals(col.getFieldName())) {
                sb.append("    @TableField(value = \"").append(col.getColumnName()).append("\", fill = FieldFill.")
                  .append("create_time".equals(col.getFieldName()) ? "INSERT" : "INSERT_UPDATE").append(")\n");
            } else if ("create_by".equals(col.getFieldName()) || "update_by".equals(col.getFieldName())) {
                sb.append("    @TableField(value = \"").append(col.getColumnName()).append("\", fill = FieldFill.")
                  .append("create_by".equals(col.getFieldName()) ? "INSERT" : "INSERT_UPDATE").append(")\n");
            } else {
                sb.append("    @TableField(\"").append(col.getColumnName()).append("\")\n");
            }
            sb.append("    @Parameter(description = \"").append(col.getColumnComment()).append("\")\n");
            sb.append("    private ").append(col.getFieldType()).append(" ").append(col.getFieldName()).append(";\n\n");
        }

        // Getter Setter
        for (TableColumnInfo col : columns) {
            String fieldName = col.getFieldName();
            String fieldType = col.getFieldType();
            String capName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            sb.append("    public ").append(fieldType).append(" get").append(capName).append("() { return ").append(fieldName).append("; }\n");
            sb.append("    public void set").append(capName).append("(").append(fieldType).append(" ").append(fieldName).append(") { this.").append(fieldName).append(" = ").append(fieldName).append("; }\n\n");
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 生成 Mapper 接口
     */
    private String generateMapper(TableInfo table, List<TableColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        String className = table.getClassName();
        String mapperName = className.replace("Sys", "") + "Mapper";
        String packageName = "com.micro.platform.system.mapper";

        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n");
        sb.append("import ").append("com.micro.platform.system.entity.").append(className).append(";\n");
        sb.append("import org.apache.ibatis.annotations.Mapper;\n\n");
        sb.append("@Mapper\n");
        sb.append("public interface ").append(mapperName).append(" extends BaseMapper<").append(className).append("> {\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 生成 Mapper XML
     */
    private String generateMapperXml(TableInfo table, List<TableColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        String className = table.getClassName();
        String mapperName = className.replace("Sys", "") + "Mapper";

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        sb.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\" >\n");
        sb.append("<mapper namespace=\"com.micro.platform.system.mapper.").append(mapperName).append("\">\n\n");
        sb.append("    <!-- 基础 ResultMap -->\n");
        sb.append("    <resultMap id=\"BaseResultMap\" type=\"com.micro.platform.system.entity.").append(className).append("\">\n");
        for (TableColumnInfo col : columns) {
            sb.append("        <result column=\"").append(col.getColumnName()).append("\" property=\"").append(col.getFieldName()).append("\" />\n");
        }
        sb.append("    </resultMap>\n\n");
        sb.append("    <!-- 基础列 -->\n");
        sb.append("    <sql id=\"Base_Column_List\">\n");
        for (int i = 0; i < columns.size(); i++) {
            sb.append("        ").append(columns.get(i).getColumnName());
            if (i < columns.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    </sql>\n");
        sb.append("</mapper>\n");
        return sb.toString();
    }

    /**
     * 生成 Service 接口
     */
    private String generateService(TableInfo table, List<TableColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        String className = table.getClassName();
        String serviceName = className.replace("Sys", "") + "Service";
        String packageName = "com.micro.platform.system.service";

        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.extension.service.IService;\n");
        sb.append("import ").append("com.micro.platform.system.entity.").append(className).append(";\n");
        sb.append("import java.util.List;\n\n");
        sb.append("public interface ").append(serviceName).append(" extends IService<").append(className).append("> {\n\n");
        sb.append("    /**\n");
        sb.append("     * 查询").append(table.getTableComment()).append("列表\n");
        sb.append("     * @param ").append(className.substring(0, 1).toLowerCase() + className.substring(1)).append(" ").append(className).append("对象\n");
        sb.append("     * @return ").append(table.getTableComment()).append("列表\n");
        sb.append("     */\n");
        sb.append("    List<").append(className).append("> list(").append(className).append(" ").append(className.substring(0, 1).toLowerCase() + className.substring(1)).append(");\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 生成 Service 实现类
     */
    private String generateServiceImpl(TableInfo table, List<TableColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        String className = table.getClassName();
        String serviceName = className.replace("Sys", "") + "Service";
        String packageName = "com.micro.platform.system.service.impl";

        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;\n");
        sb.append("import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n");
        sb.append("import ").append("com.micro.platform.system.entity.").append(className).append(";\n");
        sb.append("import ").append("com.micro.platform.system.mapper.").append(className.replace("Sys", "")).append("Mapper;\n");
        sb.append("import ").append("com.micro.platform.system.service.").append(serviceName).append(";\n");
        sb.append("import org.springframework.stereotype.Service;\n");
        sb.append("import java.util.List;\n\n");
        sb.append("@Service\n");
        sb.append("public class ").append(serviceName).append("Impl extends ServiceImpl<").append(className.replace("Sys", "")).append("Mapper, ").append(className).append("> implements ").append(serviceName).append(" {\n\n");
        sb.append("    @Override\n");
        sb.append("    public List<").append(className).append("> list(").append(className).append(" ").append(className.substring(0, 1).toLowerCase() + className.substring(1)).append(") {\n");
        sb.append("        LambdaQueryWrapper<").append(className).append("> wrapper = new LambdaQueryWrapper<>();\n");

        // 添加查询条件
        for (TableColumnInfo col : columns) {
            if (col.getIsQueryField() && !col.getIsPrimaryKey()) {
                String fieldName = col.getFieldName();
                String capName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                sb.append("        wrapper.eq(").append(className).append("::get").append(capName).append(", ").append(className.substring(0, 1).toLowerCase() + className.substring(1)).append(".get").append(capName).append("());\n");
            }
        }

        sb.append("        return list(wrapper);\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 生成 Controller 类
     */
    private String generateController(TableInfo table, List<TableColumnInfo> columns) {
        StringBuilder sb = new StringBuilder();
        String className = table.getClassName();
        String controllerName = className.replace("Sys", "") + "Controller";
        String serviceName = className.replace("Sys", "") + "Service";
        String packageName = "com.micro.platform.system.controller";
        String lowerClassName = className.substring(0, 1).toLowerCase() + className.substring(1);

        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n");
        sb.append("import com.micro.platform.common.core.result.Result;\n");
        sb.append("import ").append("com.micro.platform.system.entity.").append(className).append(";\n");
        sb.append("import ").append("com.micro.platform.system.service.").append(serviceName).append(";\n");
        sb.append("import io.swagger.v3.oas.annotations.Operation;\n");
        sb.append("import io.swagger.v3.oas.annotations.tags.Tag;\n");
        sb.append("import org.springframework.security.access.prepost.PreAuthorize;\n");
        sb.append("import org.springframework.web.bind.annotation.*;\n");
        sb.append("import java.util.List;\n\n");
        sb.append("@Tag(name = \"").append(table.getTableComment()).append("管理\", description = \"").append(table.getTableComment()).append("相关操作\")\n");
        sb.append("@RestController\n");
        sb.append("@RequestMapping(\"/system/").append(lowerClassName).append("\")\n");
        sb.append("public class ").append(controllerName).append(" {\n\n");
        sb.append("    private final ").append(serviceName).append(" ").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(";\n\n");
        sb.append("    public ").append(controllerName).append("(").append(serviceName).append(" ").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(") {\n");
        sb.append("        this.").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(" = ").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(";\n");
        sb.append("    }\n\n");

        // list 方法
        sb.append("    @Operation(summary = \"查询").append(table.getTableComment()).append("列表\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('system:").append(lowerClassName).append(":list')\")\n");
        sb.append("    @GetMapping(\"/list\")\n");
        sb.append("    public Result<List<").append(className).append(">> list(").append(className).append(" ").append(lowerClassName).append(") {\n");
        sb.append("        return Result.success(").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(".list(").append(lowerClassName).append("));\n");
        sb.append("    }\n\n");

        // query 方法
        sb.append("    @Operation(summary = \"分页查询").append(table.getTableComment()).append("列表\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('system:").append(lowerClassName).append(":query')\")\n");
        sb.append("    @GetMapping(\"/page\")\n");
        sb.append("    public Result<Page<").append(className).append(">> page(").append(className).append(" ").append(lowerClassName).append(",\n");
        sb.append("            @RequestParam(defaultValue = \"1\") Integer pageNum,\n");
        sb.append("            @RequestParam(defaultValue = \"10\") Integer pageSize) {\n");
        sb.append("        return Result.success(").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(".page(new Page<>(pageNum, pageSize),\n");
        sb.append("                new LambdaQueryWrapper<>(").append(lowerClassName).append("));\n");
        sb.append("    }\n\n");

        // getById 方法
        sb.append("    @Operation(summary = \"获取").append(table.getTableComment()).append("详情\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('system:").append(lowerClassName).append(":query')\")\n");
        sb.append("    @GetMapping(\"/{id}\")\n");
        sb.append("    public Result<").append(className).append("> getById(@PathVariable Long id) {\n");
        sb.append("        return Result.success(").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(".getById(id));\n");
        sb.append("    }\n\n");

        // add 方法
        sb.append("    @Operation(summary = \"新增").append(table.getTableComment()).append("\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('system:").append(lowerClassName).append(":add')\")\n");
        sb.append("    @PostMapping\n");
        sb.append("    public Result<Boolean> add(@RequestBody ").append(className).append(" ").append(lowerClassName).append(") {\n");
        sb.append("        return Result.success(").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(".save(").append(lowerClassName).append("));\n");
        sb.append("    }\n\n");

        // edit 方法
        sb.append("    @Operation(summary = \"修改").append(table.getTableComment()).append("\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('system:").append(lowerClassName).append(":edit')\")\n");
        sb.append("    @PutMapping\n");
        sb.append("    public Result<Boolean> edit(@RequestBody ").append(className).append(" ").append(lowerClassName).append(") {\n");
        sb.append("        return Result.success(").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(".updateById(").append(lowerClassName).append("));\n");
        sb.append("    }\n\n");

        // remove 方法
        sb.append("    @Operation(summary = \"删除").append(table.getTableComment()).append("\")\n");
        sb.append("    @PreAuthorize(\"hasAuthority('system:").append(lowerClassName).append(":remove')\")\n");
        sb.append("    @DeleteMapping(\"/{ids}\")\n");
        sb.append("    public Result<Boolean> remove(@PathVariable List<Long> ids) {\n");
        sb.append("        return Result.success(").append(serviceName.substring(0, 1).toLowerCase() + serviceName.substring(1)).append(".removeByIds(ids));\n");
        sb.append("    }\n");

        sb.append("}\n");
        return sb.toString();
    }
}
