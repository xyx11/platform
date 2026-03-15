package com.micro.platform.system.service;

import com.micro.platform.system.entity.FormDefinition;
import com.micro.platform.system.mapper.FormDefinitionMapper;
import com.micro.platform.system.service.impl.FormDefinitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
    void setUp() throws Exception {
        // 通过反射设置 baseMapper
        Class<?> clazz = formDefinitionService.getClass();
        Field baseMapperField = null;
        while (clazz != null && baseMapperField == null) {
            try {
                baseMapperField = clazz.getDeclaredField("baseMapper");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (baseMapperField != null) {
            baseMapperField.setAccessible(true);
            baseMapperField.set(formDefinitionService, formDefinitionMapper);
        }

        testForm = new FormDefinition();
        testForm.setId(1L);
        testForm.setFormName("测试表单");
        testForm.setFormCode("test_form");
        testForm.setFormType(1);
        testForm.setFormSchema("{\"type\":\"object\",\"properties\":{\"title\":{\"type\":\"string\"}}}");
        testForm.setStatus(0); // 草稿状态
    }

    @Test
    void testSelectByCode() {
        // 安排
        when(formDefinitionMapper.selectOne(any(), anyBoolean())).thenReturn(testForm);

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
        when(formDefinitionMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        // 执行
        FormDefinition result = formDefinitionService.selectByCode("not_exist");

        // 验证
        assertNull(result);
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