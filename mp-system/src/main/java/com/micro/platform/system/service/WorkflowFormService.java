package com.micro.platform.system.service;

import com.micro.platform.system.entity.WorkflowFormBinding;

import java.util.List;
import java.util.Map;

/**
 * 工作流表单服务接口
 */
public interface WorkflowFormService {

    /**
     * 获取表单绑定列表
     */
    List<WorkflowFormBinding> getFormBindings(String processDefinitionKey, String taskDefinitionKey);

    /**
     * 获取流程的所有表单绑定
     */
    List<WorkflowFormBinding> getProcessBindings(String processDefinitionKey);

    /**
     * 绑定表单
     */
    void bindForm(WorkflowFormBinding binding);

    /**
     * 解除绑定
     */
    void unbindForm(String processDefinitionKey, String taskDefinitionKey);

    /**
     * 获取可用的表单列表
     */
    List<Map<String, Object>> getAvailableForms(String processDefinitionKey);

    /**
     * 获取启动表单
     */
    Map<String, Object> getStartForm(String processKey);

    /**
     * 获取任务表单
     */
    Map<String, Object> getTaskForm(String taskDefinitionKey);
}
