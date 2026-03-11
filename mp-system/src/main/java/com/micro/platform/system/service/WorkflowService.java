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

    /**
     * 部署流程定义
     *
     * @param name 流程名称
     * @param bpmnXml BPMN XML 内容
     * @return 部署结果
     */
    Map<String, Object> deployProcessDefinition(String name, String bpmnXml);

    /**
     * 保存流程定义
     *
     * @param name 流程名称
     * @param bpmnXml BPMN XML 内容
     * @param category 流程分类
     */
    void saveProcessDefinition(String name, String bpmnXml, String category);

    /**
     * 获取流程定义列表
     *
     * @param category 流程分类
     * @return 流程定义列表
     */
    List<Map<String, Object>> getProcessDefinitions(String category);

    /**
     * 获取流程定义详情
     *
     * @param processDefinitionId 流程定义 ID
     * @return 流程定义详情
     */
    Map<String, Object> getProcessDefinition(String processDefinitionId);

    /**
     * 删除流程定义
     *
     * @param deploymentId 部署 ID
     */
    void deleteProcessDefinition(String deploymentId);

    /**
     * 获取流程定义 BPMN XML
     *
     * @param processDefinitionId 流程定义 ID
     * @return BPMN XML 内容
     */
    String getProcessDefinitionBpmn(String processDefinitionId);

    /**
     * 获取运行中的流程实例列表
     *
     * @param processDefinitionKey 流程定义 key（可选）
     * @param businessKey 业务 key（可选）
     * @return 流程实例列表
     */
    List<Map<String, Object>> getRunningProcessInstances(String processDefinitionKey, String businessKey);

    /**
     * 获取流程实例详情
     *
     * @param processInstanceId 流程实例 ID
     * @return 流程实例详情
     */
    Map<String, Object> getProcessInstance(String processInstanceId);

    /**
     * 获取流程历史活动（轨迹）
     *
     * @param processInstanceId 流程实例 ID
     * @return 历史活动列表
     */
    List<Map<String, Object>> getProcessInstanceHistory(String processInstanceId);

    /**
     * 获取流程实例统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getProcessInstanceStats();
}