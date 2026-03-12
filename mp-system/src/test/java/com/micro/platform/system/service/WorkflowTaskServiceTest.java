package com.micro.platform.system.service;

import com.micro.platform.system.service.impl.WorkflowTaskServiceImpl;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 工作流任务服务测试
 */
@ExtendWith(MockitoExtension.class)
class WorkflowTaskServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private WorkflowTaskServiceImpl workflowTaskService;

    @Mock
    private Task task;

    @Mock
    private HistoricTaskInstance historicTaskInstance;

    @Mock
    private org.flowable.task.api.TaskQuery taskQuery;

    @Mock
    private org.flowable.task.api.history.HistoricTaskInstanceQuery historicTaskQuery;

    private String testUserId;
    private String testTaskId;
    private String testProcessInstanceId;

    @BeforeEach
    void setUp() {
        testUserId = "test-user";
        testTaskId = "test-task-id";
        testProcessInstanceId = "test-process-instance-id";
    }

    @Test
    @DisplayName("获取待办任务列表 - 成功")
    void getTodoTasks_Success() {
        // 准备测试数据
        List<Task> taskList = new ArrayList<>();
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(testUserId)).thenReturn(taskQuery);
        when(taskQuery.orderByTaskPriority()).thenReturn(taskQuery);
        when(taskQuery.desc()).thenReturn(taskQuery);
        when(taskQuery.orderByTaskCreateTime()).thenReturn(taskQuery);
        when(taskQuery.asc()).thenReturn(taskQuery);
        when(taskQuery.listPage(0, 10)).thenReturn(taskList);

        // 执行测试
        List<Map<String, Object>> result = workflowTaskService.getTodoTasks(testUserId, 1, 10);

        // 验证结果
        assertNotNull(result);
        verify(taskService).createTaskQuery();
        verify(taskQuery).taskAssignee(testUserId);
    }

    @Test
    @DisplayName("获取已办任务列表 - 成功")
    void getDoneTasks_Success() {
        // 准备测试数据
        List<HistoricTaskInstance> taskList = new ArrayList<>();
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.taskAssignee(testUserId)).thenReturn(historicTaskQuery);
        when(historicTaskQuery.finished()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.desc()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.listPage(0, 10)).thenReturn(taskList);

        // 执行测试
        List<Map<String, Object>> result = workflowTaskService.getDoneTasks(testUserId, 1, 10);

        // 验证结果
        assertNotNull(result);
        verify(historyService).createHistoricTaskInstanceQuery();
        verify(historicTaskQuery).taskAssignee(testUserId);
        verify(historicTaskQuery).finished();
    }

    @Test
    @DisplayName("获取任务详情 - 成功")
    void getTask_Success() {
        // 准备测试数据
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(testTaskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(task.getId()).thenReturn(testTaskId);
        when(task.getName()).thenReturn("测试任务");
        when(task.getAssignee()).thenReturn(testUserId);
        when(task.getProcessInstanceId()).thenReturn(testProcessInstanceId);
        when(runtimeService.getVariables(testProcessInstanceId)).thenReturn(Map.of("key1", "value1"));

        // 执行测试
        Map<String, Object> result = workflowTaskService.getTask(testTaskId);

        // 验证结果
        assertNotNull(result);
        assertEquals(testTaskId, result.get("taskId"));
        assertEquals("测试任务", result.get("taskName"));
        assertTrue(result.containsKey("variables"));
    }

    @Test
    @DisplayName("获取任务详情 - 任务不存在")
    void getTask_NotFound() {
        // 准备测试数据
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(testTaskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            workflowTaskService.getTask(testTaskId);
        });
    }

    @Test
    @DisplayName("完成任务 - 成功")
    void completeTask_Success() {
        // 准备测试数据
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(testTaskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(task.getAssignee()).thenReturn(testUserId);

        // 执行测试
        assertDoesNotThrow(() -> {
            workflowTaskService.completeTask(testTaskId, testUserId, Map.of("approved", true));
        });

        // 验证
        verify(taskService).complete(eq(testTaskId), anyMap());
    }

    @Test
    @DisplayName("转办任务 - 成功")
    void transferTask_Success() {
        // 准备测试数据
        String newAssignee = "new-user";
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(testTaskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);

        // 执行测试
        assertDoesNotThrow(() -> {
            workflowTaskService.transferTask(testTaskId, testUserId, newAssignee);
        });

        // 验证
        verify(taskService).setAssignee(testTaskId, newAssignee);
    }

    @Test
    @DisplayName("委派任务 - 成功")
    void delegateTask_Success() {
        // 准备测试数据
        String delegateUser = "delegate-user";
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(testTaskId)).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);

        // 执行测试
        assertDoesNotThrow(() -> {
            workflowTaskService.delegateTask(testTaskId, testUserId, delegateUser);
        });

        // 验证
        verify(taskService).setOwner(testTaskId, testUserId);
        verify(taskService).setAssignee(testTaskId, delegateUser);
    }

    @Test
    @DisplayName("获取任务历史 - 成功")
    void getTaskHistory_Success() {
        // 准备测试数据
        List<HistoricTaskInstance> historyList = new ArrayList<>();
        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.processInstanceId(testProcessInstanceId)).thenReturn(historicTaskQuery);
        when(historicTaskQuery.orderByHistoricTaskInstanceEndTime()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.asc()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.list()).thenReturn(historyList);

        // 执行测试
        List<Map<String, Object>> result = workflowTaskService.getTaskHistory(testProcessInstanceId);

        // 验证结果
        assertNotNull(result);
        verify(historyService).createHistoricTaskInstanceQuery();
        verify(historicTaskQuery).processInstanceId(testProcessInstanceId);
    }

    @Test
    @DisplayName("获取任务统计 - 成功")
    void getTaskStats_Success() {
        // 准备测试数据
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(testUserId)).thenReturn(taskQuery);
        when(taskQuery.count()).thenReturn(5L);

        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.taskAssignee(testUserId)).thenReturn(historicTaskQuery);
        when(historicTaskQuery.finished()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.count()).thenReturn(10L);

        // 执行测试
        Map<String, Object> result = workflowTaskService.getTaskStats(testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(5L, result.get("todoCount"));
        assertEquals(10L, result.get("doneCount"));
    }

    @Test
    @DisplayName("获取任务统计 - 按优先级统计")
    void getTaskStats_ByPriority() {
        // 准备测试数据
        Task highPriorityTask = mock(Task.class);
        Task normalPriorityTask = mock(Task.class);
        Task lowPriorityTask = mock(Task.class);
        when(highPriorityTask.getPriority()).thenReturn(100);
        when(normalPriorityTask.getPriority()).thenReturn(50);
        when(lowPriorityTask.getPriority()).thenReturn(10);

        List<Task> allTasks = Arrays.asList(highPriorityTask, normalPriorityTask, lowPriorityTask);

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(testUserId)).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(allTasks);

        when(historyService.createHistoricTaskInstanceQuery()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.taskAssignee(testUserId)).thenReturn(historicTaskQuery);
        when(historicTaskQuery.finished()).thenReturn(historicTaskQuery);
        when(historicTaskQuery.count()).thenReturn(0L);

        // 执行测试
        Map<String, Object> result = workflowTaskService.getTaskStats(testUserId);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("highPriorityCount"));
        assertTrue(result.containsKey("normalPriorityCount"));
        assertTrue(result.containsKey("lowPriorityCount"));
    }
}