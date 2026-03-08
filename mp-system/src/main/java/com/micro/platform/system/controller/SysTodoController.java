package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.system.entity.SysTodo;
import com.micro.platform.system.service.SysTodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 待办事项控制器
 */
@Tag(name = "待办事项", description = "待办事项管理")
@RestController
@RequestMapping("/system/todo")
public class SysTodoController {

    private final SysTodoService sysTodoService;

    public SysTodoController(SysTodoService sysTodoService) {
        this.sysTodoService = sysTodoService;
    }

    @Operation(summary = "获取待办事项列表")
    @PreAuthorize("hasAuthority('system:todo:query')")
    @GetMapping("/list")
    public Result<Page<SysTodo>> list(SysTodo todo,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysTodo> page = sysTodoService.selectTodoPage(todo, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取我的待办列表")
    @GetMapping("/my/list")
    public Result<Page<SysTodo>> myList(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysTodo> page = sysTodoService.getMyTodos(pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取待办详情")
    @PreAuthorize("hasAuthority('system:todo:query')")
    @GetMapping("/{id}")
    public Result<SysTodo> get(@PathVariable Long id) {
        return Result.success(sysTodoService.getById(id));
    }

    @Operation(summary = "新增待办事项")
    @OperationLog(module = "待办事项", type = OperationType.INSERT, description = "新增待办事项")
    @PreAuthorize("hasAuthority('system:todo:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysTodo todo) {
        sysTodoService.save(todo);
        return Result.success();
    }

    @Operation(summary = "修改待办事项")
    @OperationLog(module = "待办事项", type = OperationType.UPDATE, description = "修改待办事项")
    @PreAuthorize("hasAuthority('system:todo:edit')")
    @PutMapping
    public Result<Void> edit(@RequestBody SysTodo todo) {
        sysTodoService.updateById(todo);
        return Result.success();
    }

    @Operation(summary = "完成待办")
    @OperationLog(module = "待办事项", type = OperationType.UPDATE, description = "完成待办")
    @PutMapping("/complete/{id}")
    public Result<Void> complete(@PathVariable Long id) {
        sysTodoService.completeTodo(id);
        return Result.success();
    }

    @Operation(summary = "取消待办")
    @OperationLog(module = "待办事项", type = OperationType.UPDATE, description = "取消待办")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        sysTodoService.cancelTodo(id);
        return Result.success();
    }

    @Operation(summary = "删除待办事项")
    @OperationLog(module = "待办事项", type = OperationType.DELETE, description = "删除待办事项")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysTodoService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "批量删除待办事项")
    @OperationLog(module = "待办事项", type = OperationType.DELETE, description = "批量删除待办事项")
    @PreAuthorize("hasAuthority('system:todo:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        sysTodoService.removeByIds(ids);
        return Result.success();
    }

    @Operation(summary = "获取待办统计信息")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(sysTodoService.getTodoStats());
    }
}