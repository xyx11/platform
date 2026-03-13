package com.micro.platform.system.service;

import java.util.List;
import java.util.Map;

/**
 * 工作流统计服务
 */
public interface WorkflowStatsService {

    /**
     * 获取任务统计概览
     */
    Map<String, Object> getOverviewStats(String userId);

    /**
     * 获取任务趋势统计（按日期）
     */
    List<Map<String, Object>> getTaskTrendStats(String userId, String startDate, String endDate);

    /**
     * 获取流程定义统计
     */
    List<Map<String, Object>> getProcessDefinitionStats(String userId);

    /**
     * 获取任务耗时统计
     */
    List<Map<String, Object>> getTaskDurationStats(String processInstanceId);

    /**
     * 获取用户任务完成情况
     */
    Map<String, Object> getUserTaskCompletionStats(String userId, String startDate, String endDate);

    /**
     * 获取部门任务统计
     */
    List<Map<String, Object>> getDeptTaskStats(String deptId, String startDate, String endDate);
}