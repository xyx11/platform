package com.micro.platform.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.platform.common.core.result.Result;
import com.micro.platform.system.entity.WfFormBinding;
import com.micro.platform.system.service.FormDefinitionService;
import com.micro.platform.system.service.WorkflowFormService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工作流表单 Controller 测试
 */
@WebMvcTest(WorkflowFormController.class)
class WorkflowFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowFormService workflowFormService;

    @MockBean
    private FormDefinitionService formDefinitionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, String> bindParams;

    @BeforeEach
    void setUp() {
        bindParams = new HashMap<>();
        bindParams.put("processDefinitionKey", "test_process");
        bindParams.put("taskDefinitionKey", "test_task");
        bindParams.put("formCode", "test_form");
        bindParams.put("formType", "1");
    }

    @Test
    void testBindForm() throws Exception {
        // 执行 & 验证
        mockMvc.perform(post("/system/workflow-form/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bindParams)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));

        verify(workflowFormService).bindFormToProcess(
            eq("test_process"), eq("test_task"), eq("test_form"), eq(1));
    }

    @Test
    void testGetBoundForm() throws Exception {
        // 执行 & 验证
        mockMvc.perform(get("/system/workflow-form/bound-form")
                .param("processDefinitionKey", "test_process")
                .param("taskDefinitionKey", "test_task")
                .param("formType", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetTaskForm() throws Exception {
        // 执行 & 验证
        mockMvc.perform(get("/system/workflow-form/task-form")
                .param("taskId", "task_123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetDraft() throws Exception {
        // 执行 & 验证
        mockMvc.perform(get("/system/workflow-form/draft")
                .param("processInstanceId", "proc_123")
                .param("taskDefinitionKey", "test_task"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetHistory() throws Exception {
        // 执行 & 验证
        mockMvc.perform(get("/system/workflow-form/history")
                .param("processInstanceId", "proc_123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testGetFormDefinition() throws Exception {
        // 执行 & 验证
        mockMvc.perform(get("/system/workflow-form/form-definition/test_form"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}