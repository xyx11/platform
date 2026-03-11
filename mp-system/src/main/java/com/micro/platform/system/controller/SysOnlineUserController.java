package com.micro.platform.system.controller;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.stp.StpUtil;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.annotation.OperationType;
import com.micro.platform.common.security.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 在线用户管理 Controller
 */
@RestController
@RequestMapping("/system/online-user")
@Tag(name = "在线用户管理")
public class SysOnlineUserController {

    private final SaTokenDao saTokenDao;

    public SysOnlineUserController(SaTokenDao saTokenDao) {
        this.saTokenDao = saTokenDao;
    }

    @GetMapping("/list")
    @Operation(summary = "查询在线用户列表")
    @PreAuthorize("@ss.hasPermission('system:online-user:list')")
    @OperationLog(module = "在线用户", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> list(@RequestParam(required = false) String username) {
        List<Map<String, Object>> onlineUsers = new ArrayList<>();

        // 获取所有 token
        List<String> tokenList = StpUtil.searchTokenValue("", 1, 10000, false);

        Long currentUserId = SecurityUtil.getUserId();

        for (String token : tokenList) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    continue;
                }

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", loginId);
                userInfo.put("token", token);
                userInfo.put("isCurrent", loginId.equals(currentUserId));

                // 获取 token 信息（使用 SaTokenDao 获取）
                long timeout = saTokenDao.getTimeout(StpUtil.getTokenName(), token);
                userInfo.put("timeout", timeout);
                userInfo.put("expireTime", timeout > 0 ? new Date(System.currentTimeMillis() + timeout) : null);

                onlineUsers.add(userInfo);
            } catch (Exception e) {
                // 忽略无效的 token
            }
        }

        return Result.success(onlineUsers);
    }

    @PostMapping("/kickout/{userId}")
    @Operation(summary = "踢出在线用户")
    @PreAuthorize("@ss.hasPermission('system:online-user:kickout')")
    @OperationLog(module = "在线用户", type = OperationType.OTHER)
    public Result<Void> kickout(@PathVariable Long userId) {
        Long currentUserId = SecurityUtil.getUserId();

        if (userId.equals(currentUserId)) {
            return Result.error("不能踢出自己");
        }

        // 踢出用户
        StpUtil.kickout(userId);

        return Result.success();
    }

    @GetMapping("/count")
    @Operation(summary = "获取在线用户数量")
    @PreAuthorize("@ss.hasPermission('system:online-user:list')")
    public Result<Map<String, Object>> count() {
        Map<String, Object> result = new HashMap<>();
        // 使用 saTokenDao 获取 token 数量
        Long count = saTokenDao.getTokenDataTotal(StpUtil.getTokenName());
        result.put("count", count != null ? count : 0);
        return Result.success(result);
    }
}