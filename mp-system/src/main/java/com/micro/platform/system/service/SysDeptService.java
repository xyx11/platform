package com.micro.platform.system.service;

import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysDept;

import java.util.List;
import java.util.Map;

/**
 * 部门服务接口
 */
public interface SysDeptService extends IServiceX<SysDept> {

    /**
     * 获取部门树
     */
    List<SysDept> getDeptTree();

    /**
     * 根据部门 ID 获取子部门 ID 列表
     */
    List<Long> getDeptChildIds(Long deptId);

    /**
     * 导出部门数据
     */
    byte[] exportDept(SysDept dept);

    /**
     * 查询部门列表（支持条件查询）
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 查询部门树（支持条件查询）
     */
    List<SysDept> selectDeptTree(SysDept dept);

    /**
     * 获取部门统计信息
     */
    Map<String, Object> getDeptStats(Long deptId);

    /**
     * 获取部门用户数量
     */
    int getDeptUserCount(Long deptId);

    /**
     * 获取部门及子部门用户总数
     */
    int getDeptWithChildrenUserCount(Long deptId);

    /**
     * 批量导出部门数据
     */
    byte[] exportDeptBatch(List<Long> deptIds);

    /**
     * 批量删除部门
     */
    void removeBatchByIds(List<Long> deptIds);

    /**
     * 批量修改部门状态
     */
    void batchUpdateStatus(List<Long> deptIds, Integer status);

    /**
     * 修改部门状态
     */
    void updateStatus(Long deptId, Integer status);

    /**
     * 导出部门数据（包含树形结构）
     */
    byte[] exportDeptWithTree(SysDept dept);
}