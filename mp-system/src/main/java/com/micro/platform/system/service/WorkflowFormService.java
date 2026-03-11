package com.micro.platform.system.service;

import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.entity.FormData;

import java.util.List;
import java.util.Map;

/**
 * 流程表单集成服务
 *
 * 用于实现工作流与动态表单的集成
 */
public interface WorkflowFormService {

    /**
     * 将表单与流程定义关联
     *
     * @param processDefinitionKey 流程定义 Key
     * @param taskDefinitionKey 任务节点 Key
     * @param formCode 表单编码
     * @param formType 表单类型（1:启动表单 2:办理表单）
     */
    void bindFormToProcess(String processDefinitionKey, String taskDefinitionKey,
                           String formCode, Integer formType);

    /**
     * 获取流程绑定的表单
     *
     * @param processDefinitionKey 流程定义 Key
     * @param taskDefinitionKey 任务节点 Key
     * @param formType 表单类型
     * @return 表单定义
     */
    FormDefinition getBoundForm(String processDefinitionKey, String taskDefinitionKey,
                                 Integer formType);

    /**
     * 启动流程并提交表单数据
     *
     * @param processDefinitionKey 流程定义 Key
     * @param businessKey 业务主键
     * @param formCode 表单编码
     * @param formData 表单数据
     * @return 流程实例
     */
    Object startProcessWithForm(String processDefinitionKey, String businessKey,
                                String formCode, Map<String, Object> formData);

    /**
     * 完成任务并提交表单数据
     *
     * @param taskId 任务 ID
     * @param formCode 表单编码
     * @param formData 表单数据
     * @param saveDraft 是否保存草稿
     */
    void completeTaskWithForm(String taskId, String formCode,
                              Map<String, Object> formData, Boolean saveDraft);

    /**
     * 获取任务可办理的表单
     *
     * @param taskId 任务 ID
     * @return 表单定义
     */
    FormDefinition getTaskForm(String taskId);

    /**
     * 保存表单数据草稿
     *
     * @param processInstanceId 流程实例 ID
     * @param taskDefinitionKey 任务节点 Key
     * @param formCode 表单编码
     * @param formData 表单数据
     */
    void saveFormDataDraft(String processInstanceId, String taskDefinitionKey,
                           String formCode, Map<String, Object> formData);

    /**
     * 获取表单数据草稿
     *
     * @param processInstanceId 流程实例 ID
     * @param taskDefinitionKey 任务节点 Key
     * @return 表单数据
     */
    FormData getFormDataDraft(String processInstanceId, String taskDefinitionKey);

    /**
     * 获取流程历史表单数据
     *
     * @param processInstanceId 流程实例 ID
     * @return 表单数据列表
     */
    List<FormData> getProcessFormHistory(String processInstanceId);
}