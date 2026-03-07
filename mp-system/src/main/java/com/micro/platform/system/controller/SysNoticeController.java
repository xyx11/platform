package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.system.entity.SysNotice;
import com.micro.platform.system.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告控制器
 */
@Tag(name = "通知公告", description = "通知公告管理")
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;

    public SysNoticeController(SysNoticeService sysNoticeService) {
        this.sysNoticeService = sysNoticeService;
    }

    @Operation(summary = "获取通知公告列表")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/list")
    public Result<Page<SysNotice>> list(SysNotice notice,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysNotice> page = sysNoticeService.selectNoticePage(notice, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取通知公告详情")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/{id}")
    public Result<SysNotice> get(@PathVariable Long id) {
        return Result.success(sysNoticeService.getById(id));
    }

    @Operation(summary = "新增通知公告")
    @PreAuthorize("hasAuthority('system:notice:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysNotice notice) {
        sysNoticeService.save(notice);
        return Result.success();
    }

    @Operation(summary = "修改通知公告")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping
    public Result<Void> edit(@RequestBody SysNotice notice) {
        sysNoticeService.updateById(notice);
        return Result.success();
    }

    @Operation(summary = "删除通知公告")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.success();
    }
}