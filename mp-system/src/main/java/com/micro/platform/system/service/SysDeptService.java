package com.micro.platform.system.service;

import com.micro.platform.common.core.service.IServiceX;
import com.micro.platform.system.entity.SysDept;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

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
    void exportDept(HttpServletResponse response, SysDept dept);

    /**
     * 查询部门列表（支持条件查询）
     */
    List<SysDept> selectDeptList(SysDept dept);
}