package com.micro.platform.system.service.impl;

import com.micro.platform.system.entity.ServerInfo;
import com.micro.platform.system.service.ServerMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务器监控服务实现
 */
@Service
public class ServerMonitorServiceImpl implements ServerMonitorService {

    private static final Logger log = LoggerFactory.getLogger(ServerMonitorServiceImpl.class);

    private static final DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public ServerInfo getServerInfo() {
        ServerInfo serverInfo = new ServerInfo();

        // CPU 信息
        ServerInfo.CpuInfo cpuInfo = new ServerInfo.CpuInfo();
        cpuInfo.setCpuNum(Runtime.getRuntime().availableProcessors());
        cpuInfo.setCpuUsage(getCpuUsage());
        cpuInfo.setCpuModel(getCpuModel());
        serverInfo.setCpuInfo(cpuInfo);

        // 内存信息
        ServerInfo.MemoryInfo memoryInfo = new ServerInfo.MemoryInfo();
        Runtime runtime = Runtime.getRuntime();
        double totalMemory = runtime.totalMemory() / 1024.0 / 1024.0;
        double freeMemory = runtime.freeMemory() / 1024.0 / 1024.0;
        double usedMemory = totalMemory - freeMemory;
        memoryInfo.setTotalMemory(totalMemory);
        memoryInfo.setUsedMemory(usedMemory);
        memoryInfo.setFreeMemory(freeMemory);
        memoryInfo.setMemoryUsage(getMemoryUsage());
        serverInfo.setMemoryInfo(memoryInfo);

        // JVM 信息
        ServerInfo.JvmInfo jvmInfo = new ServerInfo.JvmInfo();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        jvmInfo.setJvmVersion(System.getProperty("java.version"));
        jvmInfo.setStartTime(formatDate(ManagementFactory.getRuntimeMXBean().getStartTime()));
        jvmInfo.setUptime(ManagementFactory.getRuntimeMXBean().getUptime());
        jvmInfo.setHeapMax(heapUsage.getMax() / 1024.0 / 1024.0);
        jvmInfo.setHeapUsed(heapUsage.getUsed() / 1024.0 / 1024.0);
        jvmInfo.setNonHeapUsed(nonHeapUsage.getUsed() / 1024.0 / 1024.0);

        // GC 信息
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        int gcCount = 0;
        long gcTime = 0;
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            gcCount += gcBean.getCollectionCount();
            gcTime += gcBean.getCollectionTime();
        }
        jvmInfo.setGcCount(gcCount);
        jvmInfo.setGcTime(gcTime);
        serverInfo.setJvmInfo(jvmInfo);

        // 系统信息
        ServerInfo.SystemInfo systemInfo = new ServerInfo.SystemInfo();
        systemInfo.setOsName(System.getProperty("os.name"));
        systemInfo.setOsArch(System.getProperty("os.arch"));
        systemInfo.setOsVersion(System.getProperty("os.version"));
        systemInfo.setSystemLoad(getSystemLoad());
        systemInfo.setAvailableProcessors(Runtime.getRuntime().availableProcessors());
        serverInfo.setSystemInfo(systemInfo);

        // 磁盘信息（根目录）
        ServerInfo.DiskInfo diskInfo = new ServerInfo.DiskInfo();
        File root = new File("/");
        if (root.exists()) {
            diskInfo.setPath(root.getAbsolutePath());
            diskInfo.setTotal(root.getTotalSpace() / 1024.0 / 1024.0 / 1024.0);
            diskInfo.setUsed((root.getTotalSpace() - root.getFreeSpace()) / 1024.0 / 1024.0 / 1024.0);
            diskInfo.setFree(root.getFreeSpace() / 1024.0 / 1024.0 / 1024.0);
            diskInfo.setUsage((root.getTotalSpace() - root.getFreeSpace()) * 100.0 / root.getTotalSpace());
        }
        serverInfo.setDiskInfo(diskInfo);

        return serverInfo;
    }

    @Override
    public long getSystemUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
    }

    @Override
    public double getSystemLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double load = osBean.getSystemLoadAverage();
        return load < 0 ? 0 : Double.parseDouble(df.format(load));
    }

    @Override
    public double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemCpuLoad();
        return cpuLoad < 0 ? 0 : Double.parseDouble(df.format(cpuLoad * 100));
    }

    @Override
    public double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        double totalMemory = runtime.totalMemory();
        double freeMemory = runtime.freeMemory();
        double usedMemory = totalMemory - freeMemory;
        return Double.parseDouble(df.format(usedMemory * 100 / totalMemory));
    }

    @Override
    public double getDiskUsage(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }
        long total = file.getTotalSpace();
        long free = file.getFreeSpace();
        return Double.parseDouble(df.format((total - free) * 100.0 / total));
    }

    /**
     * 获取 CPU 型号（Mac/Linux）
     */
    private String getCpuModel() {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("mac")) {
                // macOS 使用 sysctl 获取
                Process process = Runtime.getRuntime().exec("sysctl -n machdep.cpu.brand_string");
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                String line = reader.readLine();
                reader.close();
                return line != null ? line.trim() : "Unknown";
            } else if (osName.contains("linux")) {
                // Linux 读取/proc/cpuinfo
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(new java.io.FileInputStream("/proc/cpuinfo")));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("model name")) {
                        reader.close();
                        return line.split(":")[1].trim();
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            log.warn("获取 CPU 型号失败：{}", e.getMessage());
        }
        return "Unknown";
    }

    /**
     * 格式化时间戳
     */
    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date(timestamp));
    }
}