package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysFile;
import com.micro.platform.system.service.SysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件管理控制器
 */
@Tag(name = "文件管理", description = "文件管理")
@RestController
@RequestMapping("/system/file")
public class SysFileController {

    private final SysFileService sysFileService;

    public SysFileController(SysFileService sysFileService) {
        this.sysFileService = sysFileService;
    }

    @Operation(summary = "获取文件列表")
    @PreAuthorize("hasAuthority('system:file:query')")
    @GetMapping("/list")
    public Result<Page<SysFile>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysFile> page = sysFileService.selectFilePage(pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取文件详情")
    @PreAuthorize("hasAuthority('system:file:query')")
    @GetMapping("/{id}")
    public Result<SysFile> get(@PathVariable Long id) {
        return Result.success(sysFileService.getById(id));
    }

    @Operation(summary = "上传文件")
    @OperationLog(module = "文件管理", type = OperationType.INSERT, description = "上传文件")
    @PreAuthorize("hasAuthority('system:file:upload')")
    @PostMapping("/upload")
    public Result<SysFile> upload(@RequestParam("file") MultipartFile file) {
        try {
            SysFile sysFile = sysFileService.uploadFile(file);
            return Result.success(sysFile);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除文件")
    @OperationLog(module = "文件管理", type = OperationType.DELETE, description = "删除文件")
    @PreAuthorize("hasAuthority('system:file:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysFileService.removeFile(id);
        return Result.success();
    }

    @Operation(summary = "批量删除文件")
    @OperationLog(module = "文件管理", type = OperationType.DELETE, description = "批量删除文件")
    @PreAuthorize("hasAuthority('system:file:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        sysFileService.batchRemove(ids);
        return Result.success();
    }

    @Operation(summary = "根据条件查询文件列表")
    @PreAuthorize("hasAuthority('system:file:query')")
    @GetMapping("/listByCondition")
    public Result<Page<SysFile>> listByCondition(SysFile file,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysFile> page = sysFileService.selectFilePage(file, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取文件统计信息")
    @PreAuthorize("hasAuthority('system:file:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(sysFileService.getFileStats());
    }

    @Operation(summary = "导出文件数据")
    @OperationLog(module = "文件管理", type = OperationType.EXPORT, description = "导出文件数据")
    @PreAuthorize("hasAuthority('system:file:query')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(SysFile file) throws Exception {
        byte[] data = sysFileService.exportFile(file);
        String fileName = URLEncoder.encode("文件数据", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}