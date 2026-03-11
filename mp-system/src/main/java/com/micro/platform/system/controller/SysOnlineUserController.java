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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 在线用户管理 Controller
 */
@RestController
@RequestMapping("/system/online-user")
@Tag(name = "在线用户管理")
public class SysOnlineUserController {

    private final SaTokenDao saTokenDao;

    // 缓存在线用户数据，减少重复查询
    private final Map<String, CacheEntry> onlineUserCache = new ConcurrentHashMap<>();

    // 缓存过期时间（毫秒）
    private static final long CACHE_EXPIRE_TIME = 5000;

    public SysOnlineUserController(SaTokenDao saTokenDao) {
        this.saTokenDao = saTokenDao;
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final List<Map<String, Object>> data;
        final long expireTime;

        CacheEntry(List<Map<String, Object>> data, long expireTime) {
            this.data = data;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询在线用户列表")
    @PreAuthorize("@ss.hasPermission('system:online-user:list')")
    @OperationLog(module = "在线用户", type = OperationType.SELECT)
    public Result<List<Map<String, Object>>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize) {

        // 检查缓存
        String cacheKey = "online_user_list:" + (username != null ? username : "");
        CacheEntry cacheEntry = onlineUserCache.get(cacheKey);

        if (cacheEntry != null && !cacheEntry.isExpired()) {
            // 缓存命中，返回缓存数据并刷新
            refreshCache(cacheKey, cacheEntry.data);
            List<Map<String, Object>> cachedData = applyPagination(cacheEntry.data, pageNum, pageSize);
            return Result.success(cachedData);
        }

        // 缓存未命中或已过期，重新查询
        List<Map<String, Object>> onlineUsers = fetchOnlineUsers(username);

        // 更新缓存
        refreshCache(cacheKey, onlineUsers);

        // 应用分页
        List<Map<String, Object>> paginatedData = applyPagination(onlineUsers, pageNum, pageSize);

        return Result.success(paginatedData);
    }

    /**
     * 获取在线用户列表（实际查询）
     */
    private List<Map<String, Object>> fetchOnlineUsers(String username) {
        List<Map<String, Object>> onlineUsers = new ArrayList<>();
        Long currentUserId = SecurityUtil.getUserId();

        // 获取所有 token
        List<String> tokenList = StpUtil.searchTokenValue("", 1, 10000, false);

        for (String token : tokenList) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) {
                    continue;
                }

                // 用户名过滤
                if (username != null && !username.isEmpty()) {
                    // 这里需要根据实际用户服务进行过滤
                    // 暂时跳过过滤，由前端或服务层处理
                }

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", loginId);
                userInfo.put("token", token);
                userInfo.put("isCurrent", loginId.equals(currentUserId));

                // 获取 token 信息
                long timeout = saTokenDao.getTimeout(StpUtil.getTokenName(), token);
                userInfo.put("timeout", timeout);
                userInfo.put("expireTime", timeout > 0 ? new Date(System.currentTimeMillis() + timeout) : null);

                onlineUsers.add(userInfo);
            } catch (Exception e) {
                // 忽略无效的 token
            }
        }

        return onlineUsers;
    }

    /**
     * 应用分页
     */
    private List<Map<String, Object>> applyPagination(List<Map<String, Object>> data, int pageNum, int pageSize) {
        if (pageNum <= 0) pageNum = 1;
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, data.size());

        if (fromIndex >= data.size()) {
            return new ArrayList<>();
        }

        return data.subList(fromIndex, toIndex);
    }

    /**
     * 刷新缓存
     */
    private void refreshCache(String key, List<Map<String, Object>> data) {
        onlineUserCache.put(key, new CacheEntry(data, System.currentTimeMillis() + CACHE_EXPIRE_TIME));

        // 清理过期缓存
        cleanupExpiredCache();
    }

    /**
     * 清理过期缓存
     */
    private void cleanupExpiredCache() {
        onlineUserCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    @GetMapping("/count")
    @Operation(summary = "获取在线用户数量")
    @PreAuthorize("@ss.hasPermission('system:online-user:list')")
    public Result<Map<String, Object>> count() {
        // 使用缓存快速返回
        CacheEntry cacheEntry = onlineUserCache.get("online_user_list:");
        if (cacheEntry != null && !cacheEntry.isExpired()) {
            Map<String, Object> result = new HashMap<>();
            result.put("count", cacheEntry.data.size());
            return Result.success(result);
        }

        // 缓存未命中，查询并缓存
        List<Map<String, Object>> users = fetchOnlineUsers(null);
        refreshCache("online_user_list:", users);

        Map<String, Object> result = new HashMap<>();
        result.put("count", users.size());
        return Result.success(result);
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

        // 清除缓存
        onlineUserCache.clear();

        return Result.success();
    }
}