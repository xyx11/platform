package com.micro.platform.generator.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.generator.entity.TableColumnInfo;
import com.micro.platform.generator.entity.TableInfo;
import com.micro.platform.generator.service.GeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 代码生成控制器
 */
@Tag(name = "代码生成", description = "代码生成相关操作")
@RestController
@RequestMapping("/generator")
public class GeneratorController {

    private final GeneratorService generatorService;

    public GeneratorController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @Operation(summary = "查询数据库表列表")
    @GetMapping("/table/list")
    public Result<Page<TableInfo>> listTables(
            @RequestParam(required = false, defaultValue = "") String tableName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<TableInfo> tables = generatorService.listTables(tableName, pageNum, pageSize);
        Page<TableInfo> page = new Page<>(pageNum, pageSize);
        page.setRecords(tables);
        // 查询总数
        long total = generatorService.listTables(tableName, 1, 1000).size();
        page.setTotal(total);
        return Result.success(page);
    }

    @Operation(summary = "获取表的列信息")
    @GetMapping("/column/list")
    public Result<List<TableColumnInfo>> listColumns(@RequestParam(defaultValue = "") String tableName) {
        return Result.success(generatorService.listColumns(tableName));
    }

    @Operation(summary = "导入表")
    @PostMapping("/table/import")
    public Result<Void> importTables(@RequestBody List<String> tableNames) {
        generatorService.importTables(tableNames);
        return Result.success();
    }

    @Operation(summary = "预览代码")
    @GetMapping("/preview")
    public Result<Map<String, String>> preview(@RequestParam(defaultValue = "") String tableName) {
        return Result.success(generatorService.previewCode(tableName));
    }

    @Operation(summary = "下载代码")
    @GetMapping("/download")
    public void download(@RequestParam(defaultValue = "") String tableName, HttpServletResponse response) throws IOException {
        byte[] data = generatorService.downloadCode(tableName);
        if (data.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "表不存在或无法生成代码");
            return;
        }
        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tableName + ".zip\"");
        response.addHeader("Content-Length", String.valueOf(data.length));
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.getOutputStream().write(data);
    }

    @Operation(summary = "同步数据库")
    @PostMapping("/sync")
    public Result<Void> sync(@RequestParam(defaultValue = "") String tableName) {
        generatorService.syncDatabase(tableName);
        return Result.success();
    }

    @Operation(summary = "删除表")
    @DeleteMapping("/{tableId}")
    public Result<Void> delete(@PathVariable Long tableId) {
        generatorService.deleteTable(tableId);
        return Result.success();
    }
}