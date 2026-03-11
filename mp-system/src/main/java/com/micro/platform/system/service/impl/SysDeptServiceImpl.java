package com.micro.platform.system.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.micro.platform.common.core.exception.BusinessException;
import com.micro.platform.common.core.service.impl.ServiceImplX;
import com.micro.platform.system.entity.SysDept;
import com.micro.platform.system.entity.SysUser;
import com.micro.platform.system.mapper.SysDeptMapper;
import com.micro.platform.system.mapper.SysUserMapper;
import com.micro.platform.system.service.SysDeptService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
public class SysDeptServiceImpl extends ServiceImplX<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysUserMapper sysUserMapper;

    public SysDeptServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Cacheable(value = "deptList", key = "'tree'", unless = "#result == null")
    public List<SysDept> getDeptTree() {
        List<SysDept> depts = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 1)
                .eq(SysDept::getDeleted, 0)
                .orderByAsc(SysDept::getSort));
        return buildDeptTree(depts, 0L);
    }

    @Override
    public List<Long> getDeptChildIds(Long deptId) {
        List<Long> ids = new ArrayList<>();
        ids.add(deptId);
        List<SysDept> children = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)
                .eq(SysDept::getStatus, 1)
                .eq(SysDept::getDeleted, 0));
        for (SysDept child : children) {
            ids.addAll(getDeptChildIds(child.getDeptId()));
        }
        return ids;
    }

    @Override
    @Cacheable(value = "deptList", key = "'list:' + (#dept.dictType ?: 'all')", unless = "#result == null")
    public List<SysDept> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
                .eq(dept.getStatus() != null, SysDept::getStatus, dept.getStatus())
                .eq(SysDept::getDeleted, 0)
                .orderByAsc(SysDept::getSort);
        List<SysDept> deptList = baseMapper.selectList(wrapper);
        // 构建树形结构
        return buildDeptTree(deptList, 0L);
    }

    @Override
    public List<SysDept> selectDeptTree(SysDept dept) {
        return selectDeptList(dept);
    }

    @Override
    public void exportDept(HttpServletResponse response, SysDept dept) {
        try {
            List<SysDept> list = selectDeptList(dept);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("部门数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysDept.class)
                    .sheet("部门数据")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("导出部门数据失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getDeptStats(Long deptId) {
        Map<String, Object> stats = new HashMap<>();

        if (deptId == null) {
            // 统计所有部门
            long totalDeptCount = baseMapper.selectCount(null);
            long activeDeptCount = baseMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getStatus, 1));
            stats.put("totalDeptCount", totalDeptCount);
            stats.put("activeDeptCount", activeDeptCount);

            // 统计总用户数
            long totalUserCount = sysUserMapper.selectCount(null);
            stats.put("totalUserCount", totalUserCount);

            // 统计顶级部门
            List<SysDept> topDepts = list(new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getParentId, 0L)
                    .eq(SysDept::getStatus, 1));

            List<Map<String, Object>> deptStats = new ArrayList<>();
            for (SysDept d : topDepts) {
                Map<String, Object> deptStat = new HashMap<>();
                deptStat.put("deptId", d.getDeptId());
                deptStat.put("deptName", d.getDeptName());
                deptStat.put("userCount", getDeptWithChildrenUserCount(d.getDeptId()));
                deptStat.put("childrenCount", getDeptChildIds(d.getDeptId()).size() - 1);
                deptStats.add(deptStat);
            }
            stats.put("deptStats", deptStats);
        } else {
            // 统计指定部门
            SysDept dept = getById(deptId);
            if (dept == null) {
                throw new BusinessException("部门不存在");
            }

            stats.put("deptId", deptId);
            stats.put("deptName", dept.getDeptName());
            stats.put("userCount", getDeptUserCount(deptId));
            stats.put("userCountWithChildren", getDeptWithChildrenUserCount(deptId));
            stats.put("childrenCount", getDeptChildIds(deptId).size() - 1);
            stats.put("leader", dept.getLeader());
            stats.put("phone", dept.getPhone());
            stats.put("email", dept.getEmail());
        }

        return stats;
    }

    @Override
    public int getDeptUserCount(Long deptId) {
        if (deptId == null) {
            return 0;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeptId, deptId);
        return sysUserMapper.selectCount(wrapper).intValue();
    }

    @Override
    public int getDeptWithChildrenUserCount(Long deptId) {
        if (deptId == null) {
            return 0;
        }
        List<Long> deptIds = getDeptChildIds(deptId);
        if (deptIds.isEmpty()) {
            return 0;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getDeptId, deptIds);
        return sysUserMapper.selectCount(wrapper).intValue();
    }

    /**
     * 构建部门树
     */
    private List<SysDept> buildDeptTree(List<SysDept> depts, Long parentId) {
        List<SysDept> tree = new ArrayList<>();
        for (SysDept dept : depts) {
            if (String.valueOf(parentId).equals(String.valueOf(dept.getParentId()))) {
                dept.setChildren(buildDeptTree(depts, dept.getDeptId()));
                tree.add(dept);
            }
        }
        return tree;
    }

    @Override
    public void exportDeptBatch(HttpServletResponse response, List<Long> deptIds) {
        try {
            if (deptIds == null || deptIds.isEmpty()) {
                return;
            }
            List<SysDept> list = listByIds(deptIds);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("部门数据", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), SysDept.class)
                    .sheet("部门数据")
                    .doWrite(list);
        } catch (Exception e) {
            throw new RuntimeException("批量导出部门数据失败：" + e.getMessage());
        }
    }

    @Override
    @CacheEvict(value = "deptList", allEntries = true)
    public void removeBatchByIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }

        // 一次性获取所有待删除部门信息
        List<SysDept> depts = listByIds(deptIds);
        if (depts.isEmpty()) {
            return;
        }

        // 批量检查子部门
        List<Long> childDeptIds = baseMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .in(SysDept::getParentId, deptIds)
                .eq(SysDept::getDeleted, 0))
                .stream()
                .map(SysDept::getDeptId)
                .toList();

        if (!childDeptIds.isEmpty()) {
            // 获取有子部门的父部门名称
            List<SysDept> parentDepts = depts.stream()
                .filter(d -> childDeptIds.contains(d.getParentId()))
                .collect(Collectors.toList());

            if (!parentDepts.isEmpty()) {
                String names = parentDepts.stream()
                    .map(SysDept::getDeptName)
                    .collect(Collectors.joining(", "));
                throw new BusinessException("以下部门存在子部门，无法删除：" + names);
            }
        }

        // 批量检查用户
        List<SysUser> users = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .in(SysUser::getDeptId, deptIds));

        if (!users.isEmpty()) {
            // 按部门分组统计
            Map<Long, Long> userCountByDept = users.stream()
                .collect(Collectors.groupingBy(SysUser::getDeptId, Collectors.counting()));

            StringBuilder errorMsg = new StringBuilder("以下部门存在用户，无法删除：");
            List<String> deptUserInfos = new ArrayList<>();
            for (SysDept dept : depts) {
                Long count = userCountByDept.get(dept.getDeptId());
                if (count != null && count > 0) {
                    deptUserInfos.add("部门 [" + dept.getDeptName() + "] 下存在 " + count + " 名用户");
                }
            }
            throw new BusinessException(errorMsg + String.join("; ", deptUserInfos));
        }

        // 执行批量删除
        removeByIds(deptIds);
    }

    @Override
    @CacheEvict(value = "deptList", allEntries = true)
    public void batchUpdateStatus(List<Long> deptIds, Integer status) {
        if (deptIds == null || deptIds.isEmpty() || status == null) {
            return;
        }
        List<SysDept> depts = listByIds(deptIds);
        for (SysDept dept : depts) {
            dept.setStatus(status);
        }
        updateBatchById(depts);
    }

    @Override
    @CacheEvict(value = "deptList", key = "'tree'")
    public void updateStatus(Long deptId, Integer status) {
        if (deptId == null || status == null) {
            throw new BusinessException("参数错误");
        }
        SysDept dept = getById(deptId);
        if (dept == null) {
            throw new BusinessException("部门不存在");
        }
        dept.setStatus(status);
        updateById(dept);
    }

    /**
     * 导出部门数据（包含树形结构）
     */
    @Override
    public void exportDeptWithTree(HttpServletResponse response, SysDept dept) {
        try {
            List<SysDept> list = selectDeptTree(dept);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("部门数据_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 添加层级列
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (SysDept d : list) {
                convertDeptToMap(d, 0, dataList);
            }

            EasyExcel.write(response.getOutputStream())
                .head(createDeptExcelHead())
                .sheet("部门数据")
                .doWrite(dataList);
        } catch (Exception e) {
            throw new RuntimeException("导出部门数据失败：" + e.getMessage());
        }
    }

    /**
     * 递归转换部门数据为 Map
     */
    private void convertDeptToMap(SysDept dept, int level, List<Map<String, Object>> dataList) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("deptName", dept.getDeptName());
        map.put("leader", dept.getLeader());
        map.put("phone", dept.getPhone());
        map.put("email", dept.getEmail());
        map.put("status", dept.getStatus() == 1 ? "正常" : "停用");
        map.put("sort", dept.getSort());
        map.put("createTime", dept.getCreateTime());

        dataList.add(map);

        if (dept.getChildren() != null && !dept.getChildren().isEmpty()) {
            for (SysDept child : dept.getChildren()) {
                convertDeptToMap(child, level + 1, dataList);
            }
        }
    }

    /**
     * 创建 Excel 表头
     */
    private List<List<String>> createDeptExcelHead() {
        List<List<String>> head = new ArrayList<>();
        head.add(Collections.singletonList("部门名称"));
        head.add(Collections.singletonList("负责人"));
        head.add(Collections.singletonList("联系电话"));
        head.add(Collections.singletonList("邮箱"));
        head.add(Collections.singletonList("状态"));
        head.add(Collections.singletonList("排序"));
        head.add(Collections.singletonList("创建时间"));
        return head;
    }
}