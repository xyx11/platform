package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.PageResult;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysNotice;
import com.micro.platform.system.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

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
    public Result<PageResult<SysNotice>> list(SysNotice notice,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysNotice> page = sysNoticeService.selectNoticePage(notice, pageNum, pageSize);
        return Result.success(PageResult.<SysNotice>build(page));
    }

    @Operation(summary = "获取通知公告详情")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/{id}")
    public Result<SysNotice> get(@PathVariable Long id) {
        SysNotice notice = sysNoticeService.getById(id);
        // 填充已读未读人数
        if (notice != null) {
            int[] status = sysNoticeService.getReadStatus(id);
            notice.setReadCount(status[0]);
            notice.setUnreadCount(status[1]);
        }
        return Result.success(notice);
    }

    @Operation(summary = "新增通知公告")
    @OperationLog(module = "通知公告", type = OperationType.INSERT, description = "新增通知公告")
    @PreAuthorize("hasAuthority('system:notice:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysNotice notice) {
        // 如果是定时发布，需要设置发布时间
        if (notice.getTimingPublish() != null && notice.getTimingPublish() == 1 && notice.getPublishTime() == null) {
            return Result.error("定时发布需要设置发布时间");
        }
        // 如果不是定时发布，立即发布
        if (notice.getTimingPublish() == null || notice.getTimingPublish() == 0) {
            notice.setStatus(1);
        } else {
            notice.setStatus(0);
        }
        sysNoticeService.save(notice);
        return Result.success();
    }

    @Operation(summary = "修改通知公告")
    @OperationLog(module = "通知公告", type = OperationType.UPDATE, description = "修改通知公告")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping
    public Result<Void> edit(@RequestBody SysNotice notice) {
        sysNoticeService.updateById(notice);
        return Result.success();
    }

    @Operation(summary = "批量删除通知公告")
    @OperationLog(module = "通知公告", type = OperationType.DELETE, description = "批量删除通知公告")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> ids) {
        sysNoticeService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "删除通知公告")
    @OperationLog(module = "通知公告", type = OperationType.DELETE, description = "删除通知公告")
    @PreAuthorize("hasAuthority('system:notice:remove')")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        sysNoticeService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "导出通知公告")
    @OperationLog(module = "通知公告", type = OperationType.EXPORT, description = "导出通知公告")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(SysNotice notice) throws Exception {
        byte[] data = sysNoticeService.exportNotice(notice);
        String fileName = URLEncoder.encode("通知公告", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "修改公告状态")
    @OperationLog(module = "通知公告", type = OperationType.UPDATE, description = "修改公告状态")
    @PreAuthorize("hasAuthority('system:notice:edit')")
    @PutMapping("/status")
    public Result<Void> status(@RequestBody SysNotice notice) {
        sysNoticeService.updateById(notice);
        return Result.success();
    }

    @Operation(summary = "标记公告为已读")
    @PostMapping("/read/{noticeId}")
    public Result<Void> markAsRead(@PathVariable Long noticeId) {
        Long userId = SecurityUtil.getUserId();
        sysNoticeService.markAsRead(noticeId, userId);
        return Result.success();
    }

    @Operation(summary = "批量标记公告为已读")
    @PostMapping("/read/batch")
    public Result<Void> batchMarkAsRead(@RequestBody List<Long> noticeIds) {
        Long userId = SecurityUtil.getUserId();
        sysNoticeService.batchMarkAsRead(noticeIds, userId);
        return Result.success();
    }

    @Operation(summary = "获取我的未读公告列表")
    @GetMapping("/unread/list")
    public Result<PageResult<SysNotice>> unreadList(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getUserId();
        Page<SysNotice> page = sysNoticeService.getUnreadNotices(userId, pageNum, pageSize);
        return Result.success(PageResult.<SysNotice>build(page));
    }

    @Operation(summary = "获取我的已读公告列表")
    @GetMapping("/read/list")
    public Result<PageResult<SysNotice>> readList(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getUserId();
        Page<SysNotice> page = sysNoticeService.getReadNotices(userId, pageNum, pageSize);
        return Result.success(PageResult.<SysNotice>build(page));
    }

    @Operation(summary = "获取未读公告数量")
    @GetMapping("/unread/count")
    public Result<Integer> unreadCount() {
        Long userId = SecurityUtil.getUserId();
        return Result.success(sysNoticeService.countUserUnread(userId));
    }

    @Operation(summary = "获取公告阅读统计")
    @GetMapping("/stats/{noticeId}")
    public Result<Map<String, Integer>> stats(@PathVariable Long noticeId) {
        int[] status = sysNoticeService.getReadStatus(noticeId);
        return Result.success(Map.of(
            "readCount", status[0],
            "unreadCount", status[1]
        ));
    }

    @Operation(summary = "获取通知公告统计信息")
    @PreAuthorize("hasAuthority('system:notice:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getNoticeStats() {
        return Result.success(sysNoticeService.getNoticeStats());
    }
}