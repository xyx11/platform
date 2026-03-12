package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.system.entity.SysDataPermission;
import com.micro.platform.system.mapper.SysDataPermissionMapper;
import com.micro.platform.system.service.impl.SysDataPermissionServiceImpl;
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
 * 数据权限服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class SysDataPermissionServiceTest {

    @Mock
    private SysDataPermissionMapper permissionMapper;

    @InjectMocks
    private SysDataPermissionServiceImpl permissionService;

    private SysDataPermission testPermission;

    @BeforeEach
    void setUp() throws Exception {
        // 通过反射设置 baseMapper
        Field field = permissionService.getClass().getSuperclass().getSuperclass().getDeclaredField("baseMapper");
        field.setAccessible(true);
        field.set(permissionService, permissionMapper);

        testPermission = new SysDataPermission();
        testPermission.setId(1L);
        testPermission.setRoleId(100L);
        testPermission.setMenuId(200L);
        testPermission.setTableName("sys_user");
        testPermission.setPermissionType(1); // 行级
        testPermission.setRuleExpression("#userId == data.id");
        testPermission.setStatus(1);
    }

    @Test
    void testSelectPermissionPage() {
        // 安排
        Page<SysDataPermission> mockPage = new Page<>(1, 10, 5);
        mockPage.setRecords(Arrays.asList(testPermission));

        when(permissionMapper.selectPage(any(Page.class), any()))
            .thenReturn(mockPage);

        // 执行
        Page<SysDataPermission> result = permissionService.selectPermissionPage(
            new SysDataPermission(), 1, 10);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(5, result.getTotal());
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    void testSelectByRoleId() {
        // 安排
        List<SysDataPermission> mockList = Arrays.asList(testPermission);
        when(permissionMapper.selectList(any())).thenReturn(mockList);

        // 执行
        List<SysDataPermission> result = permissionService.selectByRoleId(100L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getRoleId());
    }

    @Test
    void testCreatePermission() {
        // 安排
        when(permissionMapper.insert(any())).thenReturn(1);

        // 执行
        permissionService.createPermission(testPermission);

        // 验证
        verify(permissionMapper).insert(eq(testPermission));
    }

    @Test
    void testUpdatePermission() {
        // 安排
        when(permissionMapper.updateById(any())).thenReturn(1);

        // 执行
        permissionService.updatePermission(testPermission);

        // 验证
        verify(permissionMapper).updateById(eq(testPermission));
    }

    @Test
    void testDeletePermission() {
        // 安排
        when(permissionMapper.deleteById(any())).thenReturn(1);

        // 执行
        permissionService.deletePermission(1L);

        // 验证
        verify(permissionMapper).deleteById(any());
    }

    @Test
    void testGetById() {
        // 安排
        when(permissionMapper.selectById(1L)).thenReturn(testPermission);
        // 安排

        // 执行
        SysDataPermission result = permissionService.getById(1L);

        // 验证
        assertNotNull(result);
        assertEquals("sys_user", result.getTableName());
    }
}