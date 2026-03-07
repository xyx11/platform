package com.micro.platform.common.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.security.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面
 */
@Component
@Aspect
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("@annotation(com.micro.platform.common.log.annotation.OperationLog)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        com.micro.platform.common.log.entity.OperationLog operationLog = new com.micro.platform.common.log.entity.OperationLog();

        try {
            // 获取请求
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 获取注解信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            com.micro.platform.common.log.annotation.OperationLog annotation = method.getAnnotation(com.micro.platform.common.log.annotation.OperationLog.class);

            // 设置日志信息
            operationLog.setModule(annotation.module());
            operationLog.setOperationType(annotation.type().name());
            operationLog.setDescription(annotation.description());
            operationLog.setRequestMethod(request != null ? request.getMethod() : "");
            operationLog.setRequestUrl(request != null ? request.getRequestURI() : "");
            operationLog.setCreateTime(LocalDateTime.now());

            // 操作人信息
            if (SecurityUtil.isLogin()) {
                operationLog.setOperatorId(SecurityUtil.getUserId());
                operationLog.setOperatorName(SecurityUtil.getUsername());
            }

            // 请求 IP
            if (request != null) {
                operationLog.setOperationIp(getIpAddr(request));
                operationLog.setBrowser(request.getHeader("User-Agent"));
            }

            // 记录请求参数
            if (annotation.recordParams()) {
                operationLog.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
            }

            // 执行方法
            Object result = point.proceed();

            // 记录响应结果
            if (annotation.recordResult()) {
                operationLog.setResponseResult(objectMapper.writeValueAsString(result));
            }

            // 执行时长
            long executeTime = System.currentTimeMillis() - beginTime;
            operationLog.setExecuteTime(executeTime);
            operationLog.setStatus(1);

            // 保存日志（异步）
            saveLog(operationLog);

            return result;
        } catch (Exception e) {
            operationLog.setStatus(0);
            operationLog.setErrorMsg(e.getMessage());
            operationLog.setExecuteTime(System.currentTimeMillis() - beginTime);
            saveLog(operationLog);
            throw e;
        }
    }

    /**
     * 获取 IP 地址
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (Exception e) {
                    log.error("获取 IP 地址失败", e);
                }
            }
        }
        // 多个代理的情况，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 保存日志（异步执行）
     */
    private void saveLog(com.micro.platform.common.log.entity.OperationLog operationLog) {
        // TODO: 异步保存到数据库或发送到消息队列
        log.info("操作日志：{}", operationLog.getDescription());
    }
}