package com.micro.platform.system.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.system.domain.ServerInfo;
import com.micro.platform.system.domain.RedisInfo;
import com.micro.platform.system.domain.OnlineUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 系统监控控制器
 */
@Tag(name = "系统监控", description = "系统监控管理")
@RestController
@RequestMapping("/monitor")
public class SysMonitorController {

    private final RedisTemplate<String, Object> redisTemplate;

    public SysMonitorController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Operation(summary = "获取在线用户列表")
    @PreAuthorize("hasAuthority('monitor:online:query')")
    @GetMapping("/online/list")
    public Result<List<OnlineUser>> onlineList() {
        List<OnlineUser> onlineUsers = new ArrayList<>();

        // 获取所有登录的 token
        Collection<String> tokenKeys = redisTemplate.keys("satoken:*");
        if (tokenKeys != null && !tokenKeys.isEmpty()) {
            for (String tokenKey : tokenKeys) {
                Object tokenObj = redisTemplate.opsForValue().get(tokenKey);
                if (tokenObj != null) {
                    String token = tokenKey.replace("satoken:", "");
                    SaSession session = StpUtil.getSessionByLoginId(token);
                    if (session != null) {
                        OnlineUser onlineUser = new OnlineUser();
                        onlineUser.setToken(token);
                        onlineUser.setDeptName(ObjectUtil.toString(session.get("deptName")));
                        onlineUser.setUserName(ObjectUtil.toString(session.get("userName")));
                        onlineUser.setNickName(ObjectUtil.toString(session.get("nickName")));
                        onlineUser.setIpaddr(ObjectUtil.toString(session.get("loginIp")));
                        Object loginTime = session.get("loginTime");
                        if (loginTime != null) {
                            onlineUser.setLoginTime(DateUtil.formatDateTime(new Date(Long.parseLong(loginTime.toString()))));
                        }
                        onlineUsers.add(onlineUser);
                    }
                }
            }
        }

        return Result.success(onlineUsers);
    }

    @Operation(summary = "强制下线")
    @PreAuthorize("hasAuthority('monitor:online:forceLogout')")
    @DeleteMapping("/online/{token}")
    public Result<Void> forceLogout(@PathVariable String token) {
        try {
            StpUtil.logout(token);
            return Result.success();
        } catch (Exception e) {
            throw new RuntimeException("强制下线失败：" + e.getMessage());
        }
    }

    @Operation(summary = "获取 Redis 信息")
    @PreAuthorize("hasAuthority('monitor:redis:query')")
    @GetMapping("/redis")
    public Result<RedisInfo> redisInfo() {
        RedisInfo redisInfo = new RedisInfo();

        // 获取 Redis 连接信息
        Properties info = redisTemplate.getConnectionFactory().getConnection().serverCommands().info();

        redisInfo.setVersion(ObjectUtil.toString(info.get("redis_version")));
        redisInfo.setUptime(ObjectUtil.toString(info.get("uptime_in_days")) + " 天");
        redisInfo.setConnectedClients(ObjectUtil.toString(info.get("connected_clients")));
        redisInfo.setUsedMemory(ObjectUtil.toString(info.get("used_memory_human")));
        redisInfo.setUsedMemoryPeak(ObjectUtil.toString(info.get("used_memory_peak_human")));
        redisInfo.setUsedMemoryRatio(ObjectUtil.toString(info.get("used_memory_ratio")));
        redisInfo.setMemFragmentationRatio(ObjectUtil.toString(info.get("mem_fragmentation_ratio")));
        redisInfo.setTotalCommandsProcessed(ObjectUtil.toString(info.get("total_commands_processed")));
        redisInfo.setInstantaneousOpsPerSec(ObjectUtil.toString(info.get("instantaneous_ops_per_sec")));
        redisInfo.setKeyspaceHits(ObjectUtil.toString(info.get("keyspace_hits")));
        redisInfo.setKeyspaceMisses(ObjectUtil.toString(info.get("keyspace_misses")));

        // 数据库信息
        List<RedisInfo.DbInfo> dbInfos = new ArrayList<>();
        Long dbSize = redisTemplate.getConnectionFactory().getConnection().dbSize();
        if (dbSize != null && dbSize > 0) {
            RedisInfo.DbInfo dbInfo = new RedisInfo.DbInfo();
            dbInfo.setDbName("db0");
            dbInfo.setKeysCount(dbSize.intValue());
            dbInfos.add(dbInfo);
        }
        redisInfo.setDbInfo(dbInfos);

        return Result.success(redisInfo);
    }

    @Operation(summary = "获取服务器信息")
    @PreAuthorize("hasAuthority('monitor:server:query')")
    @GetMapping("/server")
    public Result<ServerInfo> serverInfo() {
        ServerInfo serverInfo = new ServerInfo();

        try {
            // JVM 信息
            Properties props = System.getProperties();
            serverInfo.setJvmName(ObjectUtil.toString(props.getProperty("java.vm.name")));
            serverInfo.setJvmVersion(ObjectUtil.toString(props.getProperty("java.version")));
            serverInfo.setJvmStartTime(DateUtil.formatDateTime(new Date(ManagementFactory.getRuntimeMXBean().getStartTime())));
            serverInfo.setJvmRunTime(formatDuration(System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime()));
            serverInfo.setJvmHome(ObjectUtil.toString(props.getProperty("java.home")));

            // 内存信息
            Runtime runtime = Runtime.getRuntime();
            serverInfo.setMaxMemory(runtime.maxMemory() / 1024 / 1024 + " MB");
            serverInfo.setTotalMemory(runtime.totalMemory() / 1024 / 1024 + " MB");
            serverInfo.setFreeMemory(runtime.freeMemory() / 1024 / 1024 + " MB");
            serverInfo.setUsableMemory((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");

            // CPU 信息
            serverInfo.setCpuProcessors(runtime.availableProcessors() + " 核心");

            // 系统信息
            serverInfo.setOsName(ObjectUtil.toString(props.getProperty("os.name")));
            serverInfo.setOsArch(ObjectUtil.toString(props.getProperty("os.arch")));
            serverInfo.setOsVersion(ObjectUtil.toString(props.getProperty("os.version")));

            // 磁盘信息
            java.io.File[] files = java.io.File.listRoots();
            List<ServerInfo.DiskInfo> diskInfos = new ArrayList<>();
            if (files != null) {
                for (java.io.File file : files) {
                    ServerInfo.DiskInfo diskInfo = new ServerInfo.DiskInfo();
                    diskInfo.setDiskName(file.getPath());
                    diskInfo.setTotal(file.getTotalSpace() / 1024 / 1024 / 1024 + " GB");
                    diskInfo.setFree(file.getFreeSpace() / 1024 / 1024 / 1024 + " GB");
                    diskInfo.setUsed((file.getTotalSpace() - file.getFreeSpace()) / 1024 / 1024 / 1024 + " GB");
                    diskInfos.add(diskInfo);
                }
            }
            serverInfo.setDiskInfo(diskInfos);

        } catch (Exception e) {
            throw new RuntimeException("获取服务器信息失败：" + e.getMessage());
        }

        return Result.success(serverInfo);
    }

    @Operation(summary = "获取缓存统计")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/cache")
    public Result<Map<String, Object>> cacheStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Redis 命令统计
            Properties info = redisTemplate.getConnectionFactory().getConnection().serverCommands().info();
            stats.put("totalCommandsProcessed", ObjectUtil.toString(info.get("total_commands_processed")));
            stats.put("instantaneousOpsPerSec", ObjectUtil.toString(info.get("instantaneous_ops_per_sec")));
            stats.put("keyspaceHits", ObjectUtil.toString(info.get("keyspace_hits")));
            stats.put("keyspaceMisses", ObjectUtil.toString(info.get("keyspace_misses")));

            // 计算命中率
            String hitsStr = toStringOrDefault(info.get("keyspace_hits"), "0");
            String missesStr = toStringOrDefault(info.get("keyspace_misses"), "0");
            long hits = Long.parseLong(hitsStr);
            long misses = Long.parseLong(missesStr);
            double hitRate = (hits + misses) > 0 ? (double) hits / (hits + misses) * 100 : 0;
            stats.put("hitRate", String.format("%.2f", hitRate));

            // 缓存键数量
            stats.put("dbSize", redisTemplate.getConnectionFactory().getConnection().dbSize());

            // 内存使用
            stats.put("usedMemory", ObjectUtil.toString(info.get("used_memory_human")));
            stats.put("usedMemoryPeak", ObjectUtil.toString(info.get("used_memory_peak_human")));

        } catch (Exception e) {
            throw new RuntimeException("获取缓存统计失败：" + e.getMessage());
        }

        return Result.success(stats);
    }

    /**
     * 格式化时间间隔
     */
    private String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%d天 %02d:%02d:%02d", days, hours, minutes, seconds);
    }

    /**
     * 转换为字符串，如果为 null 则返回默认值
     */
    private String toStringOrDefault(Object obj, String defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        String str = obj.toString();
        return str == null || str.isEmpty() ? defaultValue : str;
    }
}