package com.micro.platform.system.controller;

import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTodoTag;
import com.micro.platform.system.service.SysTodoTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 待办事项标签控制器
 */
@Tag(name = "待办标签", description = "待办事项标签管理")
@RestController
@RequestMapping("/system/todo/tag")
public class SysTodoTagController {

    private final SysTodoTagService sysTodoTagService;

    public SysTodoTagController(SysTodoTagService sysTodoTagService) {
        this.sysTodoTagService = sysTodoTagService;
    }

    @Operation(summary = "获取用户标签列表")
    @GetMapping("/list")
    public Result<List<SysTodoTag>> list() {
        return Result.success(sysTodoTagService.getUserTags(null));
    }

    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<SysTodoTag> get(@PathVariable Long id) {
        return Result.success(sysTodoTagService.getById(id));
    }

    @Operation(summary = "新增标签")
    @OperationLog(module = "待办标签", type = OperationType.INSERT, description = "新增标签")
    @PreAuthorize("hasAuthority('system:todo:add')")
    @PostMapping
    public Result<SysTodoTag> add(@RequestBody SysTodoTag tag) {
        return Result.success(sysTodoTagService.createTag(tag));
    }

    @Operation(summary = "修改标签")
    @OperationLog(module = "待办标签", type = OperationType.UPDATE, description = "修改标签")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PutMapping
    public Result<Void> edit(@RequestBody SysTodoTag tag) {
        sysTodoTagService.updateTag(tag);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @OperationLog(module = "待办标签", type = OperationType.DELETE, description = "删除标签")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysTodoTagService.deleteTag(id);
        return Result.success();
    }

    @Operation(summary = "为待办添加标签")
    @OperationLog(module = "待办标签", type = OperationType.INSERT, description = "为待办添加标签")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PostMapping("/todo/{todoId}")
    public Result<Void> addTagsToTodo(@PathVariable Long todoId, @RequestBody List<Long> tagIds) {
        sysTodoTagService.addTagsToTodo(todoId, tagIds);
        return Result.success();
    }

    @Operation(summary = "移除待办标签")
    @OperationLog(module = "待办标签", type = OperationType.DELETE, description = "移除待办标签")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @DeleteMapping("/todo/{todoId}")
    public Result<Void> removeTagsFromTodo(@PathVariable Long todoId, @RequestBody List<Long> tagIds) {
        sysTodoTagService.removeTagsFromTodo(todoId, tagIds);
        return Result.success();
    }

    @Operation(summary = "获取待办的标签列表")
    @GetMapping("/todo/{todoId}")
    public Result<List<SysTodoTag>> getTagsByTodoId(@PathVariable Long todoId) {
        return Result.success(sysTodoTagService.getTagsByTodoId(todoId));
    }
}