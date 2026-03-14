package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysDictData;
import com.micro.platform.system.entity.SysDictType;
import com.micro.platform.system.service.SysDictDataService;
import com.micro.platform.system.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理", description = "字典类型和数据管理")
@RestController
@RequestMapping("/system/dict")
public class SysDictController {

    private final SysDictTypeService dictTypeService;
    private final SysDictDataService dictDataService;

    public SysDictController(SysDictTypeService dictTypeService, SysDictDataService dictDataService) {
        this.dictTypeService = dictTypeService;
        this.dictDataService = dictDataService;
    }

    @Operation(summary = "获取字典类型列表")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/type/list")
    public Result<Page<SysDictType>> typeList(SysDictType dictType,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysDictType> page = dictTypeService.selectDictTypePage(dictType, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取字典类型详情")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/type/{dictId}")
    public Result<SysDictType> getType(@PathVariable Long dictId) {
        return Result.success(dictTypeService.getById(dictId));
    }

    @Operation(summary = "新增字典类型")
    @OperationLog(module = "字典管理", type = OperationType.INSERT, description = "新增字典类型")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody SysDictType dictType) {
        dictType.setCreateBy(SecurityUtil.getUserId());
        dictTypeService.save(dictType);
        return Result.success();
    }

    @Operation(summary = "修改字典类型")
    @OperationLog(module = "字典管理", type = OperationType.UPDATE, description = "修改字典类型")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/type")
    public Result<Void> updateType(@RequestBody SysDictType dictType) {
        dictType.setUpdateBy(SecurityUtil.getUserId());
        dictTypeService.updateById(dictType);
        // 刷新缓存
        dictTypeService.clearCache(dictType.getDictType());
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @OperationLog(module = "字典管理", type = OperationType.DELETE, description = "删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/type/{dictId}")
    public Result<Void> removeType(@PathVariable Long dictId) {
        SysDictType dictType = dictTypeService.getById(dictId);
        if (dictType != null) {
            dictTypeService.clearCache(dictType.getDictType());
        }
        dictTypeService.removeById(dictId);
        return Result.success();
    }

    @Operation(summary = "批量删除字典类型")
    @OperationLog(module = "字典管理", type = OperationType.DELETE, description = "批量删除字典类型")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/type/batch")
    public Result<Void> batchRemoveType(@RequestBody List<Long> dictIds) {
        for (Long id : dictIds) {
            SysDictType dictType = dictTypeService.getById(id);
            if (dictType != null) {
                dictTypeService.clearCache(dictType.getDictType());
            }
        }
        dictTypeService.removeByIds(dictIds);
        return Result.success();
    }

    @Operation(summary = "导出字典数据")
    @OperationLog(module = "字典管理", type = OperationType.EXPORT, description = "导出字典数据")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/type/export")
    public ResponseEntity<byte[]> exportDictType(SysDictType dictType) throws Exception {
        byte[] data = dictTypeService.exportDictType(dictType);
        String fileName = URLEncoder.encode("字典类型数据", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "获取字典数据列表")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/data/list")
    public Result<Page<SysDictData>> dataList(SysDictData dictData,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysDictData> page = dictDataService.selectDictDataPage(dictData, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "根据类型获取字典数据")
    @GetMapping("/data/type/{dictType}")
    public Result<List<SysDictData>> getDataByType(@PathVariable String dictType) {
        return Result.success(dictDataService.selectDictDataByType(dictType));
    }

    @Operation(summary = "获取字典数据详情")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/data/{dictCode}")
    public Result<SysDictData> getData(@PathVariable Long dictCode) {
        return Result.success(dictDataService.getById(dictCode));
    }

    @Operation(summary = "新增字典数据")
    @OperationLog(module = "字典管理", type = OperationType.INSERT, description = "新增字典数据")
    @PreAuthorize("hasAuthority('system:dict:add')")
    @PostMapping("/data")
    public Result<Void> addData(@RequestBody SysDictData dictData) {
        dictData.setCreateBy(SecurityUtil.getUserId());
        dictDataService.save(dictData);
        // 刷新缓存
        dictDataService.refreshCache(dictData.getDictType());
        return Result.success();
    }

    @Operation(summary = "修改字典数据")
    @OperationLog(module = "字典管理", type = OperationType.UPDATE, description = "修改字典数据")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @PutMapping("/data")
    public Result<Void> updateData(@RequestBody SysDictData dictData) {
        dictData.setUpdateBy(SecurityUtil.getUserId());
        dictDataService.updateById(dictData);
        // 刷新缓存
        dictDataService.refreshCache(dictData.getDictType());
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @OperationLog(module = "字典管理", type = OperationType.DELETE, description = "删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/data/{dictCode}")
    public Result<Void> removeData(@PathVariable Long dictCode) {
        SysDictData dictData = dictDataService.getById(dictCode);
        if (dictData != null) {
            dictDataService.refreshCache(dictData.getDictType());
        }
        dictDataService.removeById(dictCode);
        return Result.success();
    }

    @Operation(summary = "批量删除字典数据")
    @OperationLog(module = "字典管理", type = OperationType.DELETE, description = "批量删除字典数据")
    @PreAuthorize("hasAuthority('system:dict:remove')")
    @DeleteMapping("/data/batch")
    public Result<Void> batchRemoveData(@RequestBody List<Long> dictCodes) {
        for (Long id : dictCodes) {
            SysDictData dictData = dictDataService.getById(id);
            if (dictData != null) {
                dictDataService.refreshCache(dictData.getDictType());
            }
        }
        dictDataService.removeByIds(dictCodes);
        return Result.success();
    }

    @Operation(summary = "刷新字典缓存")
    @OperationLog(module = "字典管理", type = OperationType.OTHER, description = "刷新字典缓存")
    @PreAuthorize("hasAuthority('system:dict:edit')")
    @DeleteMapping("/cache/refresh")
    public Result<Void> refreshCache() {
        dictTypeService.refreshCache();
        return Result.success();
    }

    @Operation(summary = "获取字典树形结构")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getDictTree() {
        return Result.success(dictTypeService.getDictTree());
    }

    @Operation(summary = "获取字典统计信息")
    @PreAuthorize("hasAuthority('system:dict:query')")
    @GetMapping("/stats")
    public Result<Map<String, Long>> getDictStats() {
        return Result.success(dictTypeService.getDictStats());
    }
}