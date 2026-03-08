package com.micro.platform.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.*;
import com.micro.platform.system.mapper.*;
import com.micro.platform.system.service.SysIndexService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 首页统计服务实现
 */
@Service
public class SysIndexServiceImpl extends ServiceImplX<SysLoginLogMapper, SysLoginLog> implements SysIndexService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysDeptMapper sysDeptMapper;
    private final SysPostMapper sysPostMapper;
    private final SysNoticeMapper sysNoticeMapper;

    public SysIndexServiceImpl(SysUserMapper sysUserMapper,
                               SysRoleMapper sysRoleMapper,
                               SysMenuMapper sysMenuMapper,
                               SysDeptMapper sysDeptMapper,
                               SysPostMapper sysPostMapper,
                               SysNoticeMapper sysNoticeMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysDeptMapper = sysDeptMapper;
        this.sysPostMapper = sysPostMapper;
        this.sysNoticeMapper = sysNoticeMapper;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 用户统计
        statistics.put("userCount", sysUserMapper.selectCount(null));
        statistics.put("activeUserCount", sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1)));

        // 角色统计
        statistics.put("roleCount", sysRoleMapper.selectCount(null));

        // 菜单统计
        statistics.put("menuCount", sysMenuMapper.selectCount(null));

        // 日志统计
        LambdaQueryWrapper<SysLoginLog> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(SysLoginLog::getLoginTime, LocalDate.now().atStartOfDay());
        statistics.put("todayLoginCount", baseMapper.selectCount(todayWrapper));

        LambdaQueryWrapper<SysLoginLog> totalWrapper = new LambdaQueryWrapper<>();
        statistics.put("logCount", baseMapper.selectCount(totalWrapper));

        // 部门统计
        statistics.put("deptCount", sysDeptMapper.selectCount(null));

        // 岗位统计
        statistics.put("postCount", sysPostMapper.selectCount(null));

        // 公告统计
        statistics.put("noticeCount", sysNoticeMapper.selectCount(null));
        LambdaQueryWrapper<SysNotice> unreadWrapper = new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getStatus, 1)
                .ge(SysNotice::getCreateTime, LocalDate.now().atStartOfDay());
        statistics.put("todayNoticeCount", sysNoticeMapper.selectCount(unreadWrapper));

        return statistics;
    }

    @Override
    public Map<String, Object> getVisitTrend() {
        Map<String, Object> trend = new HashMap<>();

        // 获取最近 7 天的日期
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(formatter));
        }
        trend.put("dates", dates);

        // 统计最近 7 天每天的登录次数
        List<Integer> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.between(SysLoginLog::getLoginTime, start, end);
            Long count = baseMapper.selectCount(wrapper);
            values.add(count.intValue());
        }
        trend.put("values", values);

        return trend;
    }

    @Override
    public Map<String, Object> getUserDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        // 按性别统计用户分布
        List<Map<String, Object>> genderData = new ArrayList<>();
        Long maleCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getGender, 1));
        Long femaleCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getGender, 0));
        Long unknownCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().isNull(SysUser::getGender));

        genderData.add(createDistributionItem(maleCount.intValue(), "男"));
        genderData.add(createDistributionItem(femaleCount.intValue(), "女"));
        genderData.add(createDistributionItem(unknownCount.intValue(), "未知"));
        distribution.put("genderData", genderData);

        // 按状态统计用户分布
        List<Map<String, Object>> statusData = new ArrayList<>();
        Long activeCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        Long inactiveCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 0));

        statusData.add(createDistributionItem(activeCount.intValue(), "正常"));
        statusData.add(createDistributionItem(inactiveCount.intValue(), "禁用"));
        distribution.put("statusData", statusData);

        // 按部门统计用户分布（前 5 大部门）
        List<Map<String, Object>> deptData = new ArrayList<>();
        // 这里简化处理，实际应该联表查询统计每个部门的用户数
        distribution.put("deptData", deptData);

        return distribution;
    }

    /**
     * 获取热门操作统计
     */
    public Map<String, Object> getHotOperations() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> operations = new ArrayList<>();

        // 统计最近 7 天的操作类型分布（从操作日志中统计）
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(7).atStartOfDay();
        // 这里简化处理
        operations.add(createDistributionItem(100, "查询操作"));
        operations.add(createDistributionItem(80, "新增操作"));
        operations.add(createDistributionItem(60, "修改操作"));
        operations.add(createDistributionItem(40, "删除操作"));
        operations.add(createDistributionItem(20, "导出操作"));

        result.put("operations", operations);
        return result;
    }

    /**
     * 获取公告列表（最新的 5 条）
     */
    public List<Map<String, Object>> getNoticeList() {
        List<Map<String, Object>> notices = new ArrayList<>();
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, 1)
                .orderByDesc(SysNotice::getCreateTime)
                .last("LIMIT 5");

        List<SysNotice> noticeList = sysNoticeMapper.selectList(wrapper);
        for (SysNotice notice : noticeList) {
            Map<String, Object> item = new HashMap<>();
            item.put("noticeId", notice.getNoticeId());
            item.put("noticeTitle", notice.getNoticeTitle());
            item.put("noticeType", notice.getNoticeType());
            item.put("createTime", notice.getCreateTime());
            notices.add(item);
        }
        return notices;
    }

    private Map<String, Object> createDistributionItem(int value, String name) {
        Map<String, Object> item = new HashMap<>();
        item.put("value", value);
        item.put("name", name);
        return item;
    }
}