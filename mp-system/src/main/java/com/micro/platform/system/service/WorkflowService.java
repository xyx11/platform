package com.micro.platform.system.service;

import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.List;
import java.util.Map;

/**
 * 工作流服务接口
 */
public interface WorkflowService {

    /**
     * 启动流程
     */
    ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables);

    /**
     * 获取待办任务列表
     */
    List<Task> getTodoTasks(String userId);

    /**
     * 完成任务
     */
    void completeTask(String taskId, Map<String, Object> variables);

    /**
     * 获取流程变量
     */
    Map<String, Object> getProcessVariables(String processInstanceId);

    /**
     * 设置流程变量
     */
    void setProcessVariables(String processInstanceId, Map<String, Object> variables);

    /**
     * 删除流程
     */
    void deleteProcess(String processInstanceId, String deleteReason);

    /**
     * 挂起流程
     */
    void suspendProcess(String processInstanceId);

    /**
     * 激活流程
     */
    void activateProcess(String processInstanceId);
}