package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.mapper.FormDefinitionMapper;
import com.micro.platform.system.service.impl.FormDefinitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 表单定义服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class FormDefinitionServiceTest {

    @Mock
    private FormDefinitionMapper formDefinitionMapper;

    @InjectMocks
    private FormDefinitionServiceImpl formDefinitionService;

    private FormDefinition testForm;

    @BeforeEach
    void setUp() {
        testForm = new FormDefinition();
        testForm.setId(1L);
        testForm.setFormName("测试表单");
        testForm.setFormCode("test_form");
        testForm.setFormType(1);
        testForm.setFormSchema("{\"type\":\"object\",\"properties\":{\"title\":{\"type\":\"string\"}}}");
        testForm.setStatus(0); // 草稿
    }

    @Test
    void testSelectFormDefinitionPage() {
        // 安排
        Page<FormDefinition> mockPage = new Page<>(1, 10, 3);
        mockPage.setRecords(Arrays.asList(testForm));

        when(formDefinitionMapper.selectPage(any(Page.class), any()))
            .thenReturn(mockPage);

        // 执行
        Page<FormDefinition> result = formDefinitionService.selectFormDefinitionPage(
            new FormDefinition(), 1, 10);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(3, result.getTotal());
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    void testSelectByCode() {
        // 安排
        when(formDefinitionMapper.selectOne(any())).thenReturn(testForm);

        // 执行
        FormDefinition result = formDefinitionService.selectByCode("test_form");

        // 验证
        assertNotNull(result);
        assertEquals("测试表单", result.getFormName());
        assertEquals("test_form", result.getFormCode());
    }

    @Test
    void testSelectByCode_NotFound() {
        // 安排
        when(formDefinitionMapper.selectOne(any())).thenReturn(null);

        // 执行
        FormDefinition result = formDefinitionService.selectByCode("not_exist");

        // 验证
        assertNull(result);
    }

    @Test
    void testCreateFormDefinition() {
        // 安排
        when(formDefinitionMapper.insert(any())).thenReturn(1);

        // 执行
        formDefinitionService.createFormDefinition(testForm);

        // 验证
        verify(formDefinitionMapper).insert(eq(testForm));
        assertNotNull(testForm.getCreateTime());
    }

    @Test
    void testUpdateFormDefinition() {
        // 安排
        when(formDefinitionMapper.selectById(1L)).thenReturn(testForm);
        when(formDefinitionMapper.updateById(any())).thenReturn(1);

        // 执行
        formDefinitionService.updateFormDefinition(testForm);

        // 验证
        verify(formDefinitionMapper).updateById(eq(testForm));
        assertNotNull(testForm.getUpdateTime());
    }

    @Test
    void testPublishForm() {
        // 安排
        when(formDefinitionMapper.selectById(1L)).thenReturn(testForm);

        // 执行
        formDefinitionService.publishForm(1L);

        // 验证
        assertEquals(1, testForm.getStatus()); // 已发布
        verify(formDefinitionMapper).updateById(eq(testForm));
    }

    @Test
    void testDisableForm() {
        // 安排
        when(formDefinitionMapper.selectById(1L)).thenReturn(testForm);

        // 执行
        formDefinitionService.disableForm(1L);

        // 验证
        assertEquals(2, testForm.getStatus()); // 已停用
        verify(formDefinitionMapper).updateById(eq(testForm));
    }

    @Test
    void testGetById() {
        // 安排
        when(formDefinitionMapper.selectById(1L)).thenReturn(testForm);

        // 执行
        FormDefinition result = formDefinitionService.getById(1L);

        // 验证
        assertNotNull(result);
        assertEquals("测试表单", result.getFormName());
    }
}