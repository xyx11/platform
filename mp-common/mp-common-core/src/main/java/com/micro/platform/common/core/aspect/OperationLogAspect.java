package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.OperationLog;
import com.micro.platform.common.core.util.SpElUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 * 用于拦截带有 @OperationLog 注解的方法，记录操作日志
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class OperationLogAspect {

    @Pointcut("@annotation(com.micro.platform.common.core.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        // 获取注解
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        if (operationLog == null) {
            return point.proceed();
        }

        // 获取操作日志信息
        Map<String, Object> logInfo = SpElUtils.getOperationLogInfo(operationLog, method, args);

        // 获取请求信息
        HttpServletRequest request = getRequest();
        String ip = SpElUtils.getCurrentIp();
        String uri = SpElUtils.getCurrentUri();
        Long userId = SpElUtils.getCurrentUserId();
        String username = SpElUtils.getCurrentUsername();

        // 记录操作开始时间
        long startTime = System.currentTimeMillis();

        Object result = null;
        boolean success = true;
        String errorMsg = null;

        try {
            // 执行方法
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            // 记录操作日志
            long endTime = System.currentTimeMillis();
            long耗时 = endTime - startTime;

            log.info("操作日志 - 模块：{}，类型：{}，描述：{}，操作人：{}，IP: {}，URI: {}，状态：{}，耗时：{}ms，错误：{}",
                    logInfo.get("module"),
                    logInfo.get("typeDescription"),
                    logInfo.get("description"),
                    username != null ? username : userId,
                    ip,
                    uri,
                    success ? "成功" : "失败",
                   耗时，
                    errorMsg);
        }
    }

    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
}