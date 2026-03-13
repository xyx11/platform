package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTaskAttachment;
import com.micro.platform.system.service.SysTaskAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 任务附件控制器
 */
@Tag(name = "任务附件", description = "任务附件管理")
@RestController
@RequestMapping("/system/todo/attachment")
public class SysTaskAttachmentController {

    private final SysTaskAttachmentService sysTaskAttachmentService;

    public SysTaskAttachmentController(SysTaskAttachmentService sysTaskAttachmentService) {
        this.sysTaskAttachmentService = sysTaskAttachmentService;
    }

    @Operation(summary = "获取任务附件列表")
    @GetMapping("/list")
    public Result<List<SysTaskAttachment>> list(@RequestParam Long todoId) {
        return Result.success(sysTaskAttachmentService.getAttachmentsByTodoId(todoId));
    }

    @Operation(summary = "获取附件数量")
    @GetMapping("/count")
    public Result<Integer> count(@RequestParam Long todoId) {
        return Result.success(sysTaskAttachmentService.getAttachmentCount(todoId));
    }

    @Operation(summary = "上传附件")
    @OperationLog(module = "任务附件", type = OperationType.INSERT, description = "上传附件")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PostMapping("/upload")
    public Result<SysTaskAttachment> upload(@RequestParam Long todoId,
                                            @RequestParam("file") MultipartFile file) {
        return Result.success(sysTaskAttachmentService.uploadAttachment(todoId, file));
    }

    @Operation(summary = "下载附件")
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable Long id) {
        try {
            byte[] content = sysTaskAttachmentService.downloadAttachment(id);
            ByteArrayResource resource = new ByteArrayResource(content);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attachment\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(content.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "删除附件")
    @OperationLog(module = "任务附件", type = OperationType.DELETE, description = "删除附件")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysTaskAttachmentService.deleteAttachment(id);
        return Result.success();
    }
}