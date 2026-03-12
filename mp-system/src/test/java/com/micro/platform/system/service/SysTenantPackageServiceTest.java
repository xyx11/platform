package com.micro.platform.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.micro.platform.system.entity.SysTenantPackage;
import com.micro.platform.system.mapper.SysTenantPackageMapper;
import com.micro.platform.system.service.impl.SysTenantPackageServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.math.BigDecimal;
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
 * 租户套餐服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class SysTenantPackageServiceTest {

    @Mock
    private SysTenantPackageMapper packageMapper;

    @InjectMocks
    private SysTenantPackageServiceImpl packageService;

    private SysTenantPackage testPackage;

    @BeforeEach
    void setUp() throws Exception {
        // 通过反射设置 baseMapper
        Field field = packageService.getClass().getSuperclass().getSuperclass().getDeclaredField("baseMapper");
        field.setAccessible(true);
        field.set(packageService, packageMapper);

        testPackage = new SysTenantPackage();
        testPackage.setId(1L);
        testPackage.setName("测试套餐");
        testPackage.setCode("test_package");
        testPackage.setPackageType(2);
        testPackage.setPrice(new BigDecimal("99.00"));
        testPackage.setMaxUsers(20);
        testPackage.setStatus(1);
    }

    @Test
    void testSelectPackagePage() {
        // 安排
        Page<SysTenantPackage> mockPage = new Page<>(1, 10, 3);
        mockPage.setRecords(Arrays.asList(testPackage));

        when(packageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
            .thenReturn(mockPage);

        // 执行
        Page<SysTenantPackage> result = packageService.selectPackagePage(
            new SysTenantPackage(), 1, 10);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(3, result.getTotal());
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    void testSelectAvailablePackages() {
        // 安排
        List<SysTenantPackage> mockList = Arrays.asList(testPackage);
        LambdaQueryWrapper<SysTenantPackage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantPackage::getStatus, 1);
        queryWrapper.orderByAsc(SysTenantPackage::getSort);

        when(packageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(mockList);

        // 执行
        List<SysTenantPackage> result = packageService.selectAvailablePackages();

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test_package", result.get(0).getCode());
    }

    @Test
    void testCreatePackage() {
        // 安排
        when(packageMapper.insert(any())).thenReturn(1);

        // 执行
        packageService.createPackage(testPackage);

        // 验证
        verify(packageMapper).insert(eq(testPackage));
    }

    @Test
    void testUpdatePackage() {
        // 安排
        when(packageMapper.selectById(1L)).thenReturn(testPackage);
        when(packageMapper.updateById(any())).thenReturn(1);

        // 执行
        packageService.updatePackage(testPackage);

        // 验证
        verify(packageMapper).updateById(eq(testPackage));
    }

    @Test
    void testDisablePackage() {
        // 安排
        when(packageMapper.selectById(1L)).thenReturn(testPackage);

        // 执行
        packageService.disablePackage(1L);

        // 验证
        assertEquals(0, testPackage.getStatus());
        verify(packageMapper).updateById(eq(testPackage));
    }

    @Test
    void testGetById() {
        // 安排
        when(packageMapper.selectById(1L)).thenReturn(testPackage);

        // 执行
        SysTenantPackage result = packageService.getById(1L);

        // 验证
        assertNotNull(result);
        assertEquals("测试套餐", result.getName());
    }
}