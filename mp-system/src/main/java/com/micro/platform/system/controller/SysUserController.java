package com.micro.platform.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理", description = "用户增删改查")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "获取用户列表")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/list")
    public Result<Page<SysUser>> list(SysUser user,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysUser> page = sysUserService.selectUserPage(user, pageNum, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取用户详情")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/{userId}")
    public Result<SysUser> get(@PathVariable Long userId) {
        return Result.success(sysUserService.getById(userId));
    }

    @Operation(summary = "新增用户")
    @OperationLog(module = "用户管理", type = OperationType.INSERT, description = "新增用户")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        user.setCreateBy(SecurityUtil.getUserId());
        sysUserService.saveUser(user);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "修改用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        user.setUpdateBy(SecurityUtil.getUserId());
        sysUserService.updateUser(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "删除用户")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @DeleteMapping("/{userId}")
    public Result<Void> remove(@PathVariable Long userId) {
        sysUserService.removeById(userId);
        return Result.success();
    }

    @Operation(summary = "批量删除用户")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "批量删除用户")
    @PreAuthorize("hasAuthority('system:user:remove')")
    @DeleteMapping("/batch")
    public Result<Void> batchRemove(@RequestBody List<Long> userIds) {
        sysUserService.removeByIds(userIds);
        return Result.success();
    }

    @Operation(summary = "导出用户数据")
    @OperationLog(module = "用户管理", type = OperationType.EXPORT, description = "导出用户数据")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(SysUser user) throws Exception {
        byte[] data = sysUserService.exportUser(user);
        String fileName = URLEncoder.encode("用户数据", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/downloadTemplate")
    public ResponseEntity<byte[]> downloadTemplate() throws Exception {
        byte[] data = sysUserService.downloadTemplate();
        String fileName = URLEncoder.encode("用户导入模板", "UTF-8").replaceAll("\\\\+", "%20");
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @Operation(summary = "导入用户数据")
    @OperationLog(module = "用户管理", type = OperationType.IMPORT, description = "导入用户数据")
    @PreAuthorize("hasAuthority('system:user:add')")
    @PostMapping("/import")
    public Result<Object> importUser(@RequestParam("file") MultipartFile file) {
        Object result = sysUserService.importUser(file);
        return Result.success(result);
    }

    @Operation(summary = "重置密码")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "重置密码")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        sysUserService.resetPassword(request.getUserId(), request.getPassword());
        return Result.success();
    }

    @Operation(summary = "批量重置密码")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "批量重置密码")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/password/batch")
    public Result<Void> batchResetPassword(@RequestBody Map<String, Object> params) {
        List<Long> userIds = extractLongList(params.get("userIds"));
        String password = (String) params.get("password");
        sysUserService.batchResetPassword(userIds, password);
        return Result.success();
    }

    /**
     * 从对象中提取 Long 列表（兼容 Integer、Long 和 String）
     */
    @SuppressWarnings("unchecked")
    private List<Long> extractLongList(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            return list.stream()
                .map(item -> {
                    if (item instanceof Integer) {
                        return ((Integer) item).longValue();
                    } else if (item instanceof Long) {
                        return (Long) item;
                    } else if (item instanceof String) {
                        return Long.valueOf((String) item);
                    }
                    return null;
                })
                .toList();
        }
        return null;
    }

    @Operation(summary = "解锁用户")
    @OperationLog(module = "用户管理", type = OperationType.OTHER, description = "解锁用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PostMapping("/unlock/{userId}")
    public Result<Void> unlockUser(@PathVariable Long userId) {
        sysUserService.unlockUser(userId);
        return Result.success();
    }

    @Operation(summary = "批量解锁用户")
    @OperationLog(module = "用户管理", type = OperationType.OTHER, description = "批量解锁用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PostMapping("/unlock/batch")
    public Result<Void> batchUnlockUsers(@RequestBody List<Long> userIds) {
        sysUserService.batchUnlockUsers(userIds);
        return Result.success();
    }

    @Operation(summary = "获取用户统计信息")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestParam(required = false) Long userId) {
        return Result.success(sysUserService.getUserStats(userId));
    }

    @Operation(summary = "获取用户的角色 ID 列表")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/roles/{userId}")
    public Result<Map<String, Object>> getUserRoles(@PathVariable Long userId) {
        return Result.success(sysUserService.getUserRoles(userId));
    }

    @Operation(summary = "分配角色到用户")
    @OperationLog(module = "用户管理", type = OperationType.GRANT, description = "分配角色到用户")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PostMapping("/roles")
    public Result<Void> assignRoles(@RequestParam Long userId, @RequestBody Long[] roleIds) {
        sysUserService.assignRoles(userId, roleIds);
        return Result.success();
    }

    @Operation(summary = "获取用户详情（包含部门、角色等信息）")
    @PreAuthorize("hasAuthority('system:user:query')")
    @GetMapping("/detail/{userId}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long userId) {
        return Result.success(sysUserService.getUserDetail(userId));
    }

    @Operation(summary = "修改用户状态")
    @OperationLog(module = "用户管理", type = OperationType.UPDATE, description = "修改用户状态")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        sysUserService.updateStatus(userId, status);
        return Result.success();
    }

    /**
     * 重置密码请求 DTO
     */
    public static class ResetPasswordRequest {
        private Long userId;
        private String password;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}