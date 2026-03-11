package com.micro.platform.system.service.impl;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import com.micro.platform.system.service.WorkflowService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工作流服务实现
 */
@Service
public class WorkflowServiceImpl implements WorkflowService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    public WorkflowServiceImpl(RuntimeService runtimeService,
                               TaskService taskService,
                               HistoryService historyService,
                               RepositoryService repositoryService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance startProcess(String processDefinitionKey, String businessKey, Map<String, Object> variables) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
    }

    @Override
    public List<Task> getTodoTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskPriority()
                .desc()
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcess(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendProcess(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateProcess(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "processDefinitions", allEntries = true)
    public Map<String, Object> deployProcessDefinition(String name, String bpmnXml) {
        // 部署流程
        Deployment deployment = repositoryService.createDeployment()
                .addStringInputStream(name + ".bpmn20.xml", new ByteArrayInputStream(bpmnXml.getBytes()))
                .name(name)
                .deploy();

        // 获取部署后的流程定义
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        Map<String, Object> result = new HashMap<>();
        result.put("deploymentId", deployment.getId());
        result.put("definitionId", definition.getId());
        result.put("name", definition.getName());
        result.put("key", definition.getKey());
        result.put("version", definition.getVersion());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "processDefinitions", allEntries = true)
    public void saveProcessDefinition(String name, String bpmnXml, String category) {
        // 保存流程定义（带分类）
        Deployment deployment = repositoryService.createDeployment()
                .addStringInputStream(name + ".bpmn20.xml", new ByteArrayInputStream(bpmnXml.getBytes()))
                .name(name)
                .category(category)
                .deploy();

        // 获取流程定义并设置分类
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

        // 更新流程定义分类
        if (definition != null && category != null) {
            repositoryService.setProcessDefinitionCategory(definition.getId(), category);
        }
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#category ?: 'all'", unless = "#result == null")
    public List<Map<String, Object>> getProcessDefinitions(String category) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

        if (category != null && !category.isEmpty()) {
            query.processDefinitionCategory(category);
        }

        List<ProcessDefinition> definitions = query.orderByProcessDefinitionVersion().desc().list();

        return definitions.stream().map(definition -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", definition.getId());
            map.put("name", definition.getName());
            map.put("key", definition.getKey());
            map.put("version", definition.getVersion());
            map.put("category", definition.getCategory());
            map.put("deploymentId", definition.getDeploymentId());
            map.put("resourceName", definition.getResourceName());
            map.put("description", definition.getDescription());
            map.put("suspended", definition.isSuspended());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "processDefinitions", key = "#processDefinitionId", unless = "#result == null")
    public Map<String, Object> getProcessDefinition(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", definition.getId());
        result.put("name", definition.getName());
        result.put("key", definition.getKey());
        result.put("version", definition.getVersion());
        result.put("category", definition.getCategory());
        result.put("deploymentId", definition.getDeploymentId());
        result.put("resourceName", definition.getResourceName());
        result.put("description", definition.getDescription());
        result.put("suspended", definition.isSuspended());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "processDefinitions", allEntries = true)
    public void deleteProcessDefinition(String deploymentId) {
        // true 表示级联删除
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Override
    public String getProcessDefinitionBpmn(String processDefinitionId) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if (definition == null) {
            return null;
        }

        // 获取 BPMN 资源流并读取为字符串
        String resourceName = definition.getResourceName();
        try (InputStream inputStream = repositoryService.getResourceAsStream(definition.getDeploymentId(), resourceName)) {
            if (inputStream != null) {
                byte[] bytes = inputStream.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("读取 BPMN 失败：" + e.getMessage(), e);
        }
        return null;
    }
}