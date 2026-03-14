package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysPost;
import com.micro.platform.system.service.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 岗位管理控制器
 */
@Tag(name = "岗位管理", description = "岗位增删改查")
@RestController
@RequestMapping("/system/post")
public class SysPostController {

    private final SysPostService sysPostService;

    public SysPostController(SysPostService sysPostService) {
        this.sysPostService = sysPostService;
    }

    @Operation(summary = "获取岗位列表")
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/list")
    public Result<Page<SysPost>> list(SysPost post,
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysPost> page = sysPostService.selectPostPage(post, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取所有正常岗位")
    @GetMapping("/option")
    public Result<List<SysPost>> option() {
        return Result.success(sysPostService.selectNormalPosts());
    }

    @Operation(summary = "获取岗位详情")
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/{postId}")
    public Result<SysPost> get(@PathVariable Long postId) {
        return Result.success(sysPostService.getById(postId));
    }

    @Operation(summary = "新增岗位")
    @OperationLog(module = "岗位管理", type = OperationType.INSERT, description = "新增岗位")
    @PreAuthorize("hasAuthority('system:post:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysPost post) {
        post.setCreateBy(SecurityUtil.getUserId());
        sysPostService.save(post);
        return Result.success();
    }

    @Operation(summary = "修改岗位")
    @OperationLog(module = "岗位管理", type = OperationType.UPDATE, description = "修改岗位")
    @PreAuthorize("hasAuthority('system:post:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysPost post) {
        post.setUpdateBy(SecurityUtil.getUserId());
        sysPostService.updateById(post);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @OperationLog(module = "岗位管理", type = OperationType.DELETE, description = "删除岗位")
    @PreAuthorize("hasAuthority('system:post:remove')")
    @DeleteMapping("/{postIds}")
    public Result<Void> remove(@PathVariable Long[] postIds) {
        sysPostService.removeBatchByIds(java.util.Arrays.asList(postIds));
        return Result.success();
    }

    @Operation(summary = "批量删除岗位")
    @OperationLog(module = "岗位管理", type = OperationType.DELETE, description = "批量删除岗位")
    @PreAuthorize("hasAuthority('system:post:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> postIds) {
        sysPostService.removeByIds(postIds);
        return Result.success();
    }

    @Operation(summary = "导出岗位数据")
    @OperationLog(module = "岗位管理", type = OperationType.EXPORT, description = "导出岗位数据")
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(SysPost post) throws Exception {
        byte[] data = sysPostService.exportPost(post);
        String fileName = URLEncoder.encode("岗位数据", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "获取岗位统计信息")
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) Long postId) {
        return Result.success(sysPostService.getPostStats(postId));
    }

    @Operation(summary = "获取岗位用户列表")
    @PreAuthorize("hasAuthority('system:post:query')")
    @GetMapping("/users/{postId}")
    public Result<List<Map<String, Object>>> getUsers(@PathVariable Long postId) {
        return Result.success(sysPostService.getPostUsers(postId));
    }

    @Operation(summary = "分配用户到岗位")
    @OperationLog(module = "岗位管理", type = OperationType.GRANT, description = "分配用户到岗位")
    @PreAuthorize("hasAuthority('system:post:edit')")
    @PostMapping("/users/assign")
    public Result<Void> assignUsers(@RequestParam Long postId, @RequestBody List<Long> userIds) {
        sysPostService.assignUsers(postId, userIds);
        return Result.success();
    }

    @Operation(summary = "从岗位移除用户")
    @OperationLog(module = "岗位管理", type = OperationType.OTHER, description = "从岗位移除用户")
    @PreAuthorize("hasAuthority('system:post:edit')")
    @PostMapping("/users/remove")
    public Result<Void> removeUsers(@RequestParam Long postId, @RequestBody List<Long> userIds) {
        sysPostService.removeUsers(postId, userIds);
        return Result.success();
    }
}