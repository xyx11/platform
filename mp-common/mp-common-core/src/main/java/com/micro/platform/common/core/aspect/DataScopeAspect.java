package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.DataScope;
import com.micro.platform.common.core.enums.DataScopeType;
import com.micro.platform.common.core.entity.LoginUser;
import com.micro.platform.common.core.util.SecurityUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 数据权限切面
 *
 * 拦截标注了 @DataScope 注解的方法，自动注入数据权限过滤条件
 */
@Aspect
@Component
public class DataScopeAspect {

    /**
     * 切入点：所有标注了 @DataScope 注解的方法
     */
    @Pointcut("@annotation(com.micro.platform.common.core.annotation.DataScope)")
    public void dataScopePointcut() {
    }

    /**
     * 前置通知：在方法执行前设置数据权限过滤条件
     */
    @Before("dataScopePointcut()")
    public void doBefore(JoinPoint point) {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 获取 @DataScope 注解
        DataScope dataScope = method.getAnnotation(DataScope.class);
        if (dataScope == null) {
            return;
        }

        // 获取登录用户信息
        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null) {
            return;
        }

        // 如果是超级管理员，不需要设置数据权限
        if (loginUser.getRoleCodes() != null && loginUser.getRoleCodes().contains("admin")) {
            return;
        }

        // 获取数据权限范围
        Integer dataScopeType = loginUser.getDataScope();
        if (dataScopeType == null || dataScopeType.equals(DataScopeType.ALL.getCode())) {
            return;
        }

        // 在参数中设置数据权限过滤条件
        // 注意：这里需要通过 ThreadLocal 或者参数传递的方式将过滤条件传递给 Mapper
        DataScopeContext.setDeptAlias(dataScope.deptAlias());
        DataScopeContext.setUserAlias(dataScope.userAlias());
        DataScopeContext.setLoginUser(loginUser);
    }
}