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
    public List<SysDept> getDeptTree() {
        List<SysDept> depts = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getStatus, 1)
                .orderByAsc(SysDept::getSort));
        return buildDeptTree(depts, 0L);
    }

    @Override
    public List<Long> getDeptChildIds(Long deptId) {
        List<Long> ids = new ArrayList<>();
        ids.add(deptId);
        List<SysDept> children = list(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getParentId, deptId)
                .eq(SysDept::getStatus, 1));
        for (SysDept child : children) {
            ids.addAll(getDeptChildIds(child.getDeptId()));
        }
        return ids;
    }

    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
                .eq(dept.getStatus() != null, SysDept::getStatus, dept.getStatus())
                .orderByAsc(SysDept::getSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<SysDept> selectDeptTree(SysDept dept) {
        List<SysDept> depts = selectDeptList(dept);
        return buildDeptTree(depts, 0L);
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
            for (SysDept dept : topDepts) {
                Map<String, Object> deptStat = new HashMap<>();
                deptStat.put("deptId", dept.getDeptId());
                deptStat.put("deptName", dept.getDeptName());
                deptStat.put("userCount", getDeptWithChildrenUserCount(dept.getDeptId()));
                deptStat.put("childrenCount", getDeptChildIds(dept.getDeptId()).size() - 1);
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
            if (parentId.equals(dept.getParentId())) {
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
            List<SysDept> list = list(new LambdaQueryWrapper<SysDept>().in(SysDept::getDeptId, deptIds));

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
    public void removeBatchByIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        // 检查是否有子部门
        for (Long deptId : deptIds) {
            List<SysDept> children = list(new LambdaQueryWrapper<SysDept>()
                    .eq(SysDept::getParentId, deptId));
            if (!children.isEmpty()) {
                SysDept dept = getById(deptId);
                if (dept != null) {
                    throw new BusinessException("部门 [" + dept.getDeptName() + "] 下存在子部门，无法删除");
                }
            }
        }
        removeByIds(deptIds);
    }

    @Override
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
}
