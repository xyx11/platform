package com.micro.platform.system.service;

import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.entity.WfFormBinding;
import com.micro.platform.system.mapper.WfFormBindingMapper;
import com.micro.platform.system.service.impl.WorkflowFormServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工作流表单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class WorkflowFormServiceTest {

    @Mock
    private WorkflowService workflowService;

    @Mock
    private FormDefinitionService formDefinitionService;

    @Mock
    private WfFormBindingMapper wfFormBindingMapper;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private WorkflowFormServiceImpl workflowFormService;

    private FormDefinition testFormDefinition;
    private WfFormBinding testBinding;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testFormDefinition = new FormDefinition();
        testFormDefinition.setId(1L);
        testFormDefinition.setFormName("测试表单");
        testFormDefinition.setFormCode("test_form");
        testFormDefinition.setFormSchema("{\"type\":\"object\"}");

        testBinding = new WfFormBinding();
        testBinding.setId(1L);
        testBinding.setProcessDefinitionKey("test_process");
        testBinding.setTaskDefinitionKey("test_task");
        testBinding.setFormCode("test_form");
        testBinding.setFormType(1);
        testBinding.setBindingKey("test_process:test_task:1");
    }

    @Test
    void testBindFormToProcess_FormNotFound() {
        // 安排
        when(formDefinitionService.selectByCode("not_exist")).thenReturn(null);

        // 执行 & 验证
        assertThrows(Exception.class, () -> {
            workflowFormService.bindFormToProcess("test_process", "test_task", "not_exist", 1);
        });
    }

    @Test
    void testBindFormToProcess_CreateNewBinding() {
        // 安排
        when(formDefinitionService.selectByCode("test_form")).thenReturn(testFormDefinition);
        when(wfFormBindingMapper.selectOne(any())).thenReturn(null);
        when(wfFormBindingMapper.insert(any())).thenReturn(1);

        // 执行
        workflowFormService.bindFormToProcess("test_process", "test_task", "test_form", 1);

        // 验证
        verify(formDefinitionService).selectByCode("test_form");
        verify(wfFormBindingMapper).insert(any(WfFormBinding.class));
    }

    @Test
    void testBindFormToProcess_UpdateExistingBinding() {
        // 安排
        when(formDefinitionService.selectByCode("test_form")).thenReturn(testFormDefinition);
        when(wfFormBindingMapper.selectOne(any())).thenReturn(testBinding);

        // 执行
        workflowFormService.bindFormToProcess("test_process", "test_task", "test_form", 1);

        // 验证
        verify(formDefinitionService).selectByCode("test_form");
        verify(wfFormBindingMapper).updateById(any(WfFormBinding.class));
    }

    @Test
    void testGetBoundForm_NotFound() {
        // 安排
        when(wfFormBindingMapper.selectOne(any())).thenReturn(null);

        // 执行
        FormDefinition result = workflowFormService.getBoundForm("test_process", "test_task", 1);

        // 验证
        assertNull(result);
    }

    @Test
    void testGetBoundForm_Found() {
        // 安排
        when(wfFormBindingMapper.selectOne(any())).thenReturn(testBinding);
        when(formDefinitionService.selectByCode("test_form")).thenReturn(testFormDefinition);

        // 执行
        FormDefinition result = workflowFormService.getBoundForm("test_process", "test_task", 1);

        // 验证
        assertNotNull(result);
        assertEquals("test_form", result.getFormCode());
        assertEquals("测试表单", result.getFormName());
    }

    @Test
    void testStartProcessWithForm_FormNotFound() {
        // 安排
        when(formDefinitionService.selectByCode("not_exist")).thenReturn(null);

        // 执行 & 验证
        assertThrows(Exception.class, () -> {
            workflowFormService.startProcessWithForm("test_process", "1", "not_exist", new HashMap<>());
        });
    }

    @Test
    void testStartProcessWithForm_Success() {
        // 安排
        Map<String, Object> formData = new HashMap<>();
        formData.put("field1", "value1");

        ProcessInstance mockInstance = mock(ProcessInstance.class);
        when(mockInstance.getId()).thenReturn("proc_123");
        when(mockInstance.getBusinessKey()).thenReturn("1");

        when(formDefinitionService.selectByCode("test_form")).thenReturn(testFormDefinition);
        when(workflowService.startProcess(anyString(), anyString(), anyMap())).thenReturn(mockInstance);

        // 执行
        ProcessInstance result = workflowFormService.startProcessWithForm("test_process", "1", "test_form", formData);

        // 验证
        assertNotNull(result);
        assertEquals("proc_123", result.getId());
        verify(workflowService).startProcess(eq("test_process"), eq("1"), anyMap());
    }

    @Test
    void testCompleteTaskWithForm_SaveDraft() {
        // 安排
        Task mockTask = mock(Task.class);
        when(mockTask.getProcessInstanceId()).thenReturn("proc_123");
        when(mockTask.getTaskDefinitionKey()).thenReturn("test_task");

        when(taskService.createTaskQuery()).thenReturn(mock(TaskService.TaskQuery.class));
        TaskService.TaskQuery taskQuery = mock(TaskService.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task_123")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        // 执行
        Map<String, Object> formData = new HashMap<>();
        workflowFormService.completeTaskWithForm("task_123", "test_form", formData, true);

        // 验证：保存草稿时不应调用完成任务
        verify(workflowService, never()).completeTask(anyString(), anyMap());
    }

    @Test
    void testGetTaskForm_Success() {
        // 安排
        Task mockTask = mock(Task.class);
        when(mockTask.getProcessDefinitionKey()).thenReturn("test_process");
        when(mockTask.getTaskDefinitionKey()).thenReturn("test_task");

        when(taskService.createTaskQuery()).thenReturn(mock(TaskService.TaskQuery.class));
        TaskService.TaskQuery taskQuery = mock(TaskService.TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task_123")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        when(wfFormBindingMapper.selectOne(any())).thenReturn(testBinding);
        when(formDefinitionService.selectByCode("test_form")).thenReturn(testFormDefinition);

        // 执行
        FormDefinition result = workflowFormService.getTaskForm("task_123");

        // 验证
        assertNotNull(result);
        assertEquals("test_form", result.getFormCode());
    }
}