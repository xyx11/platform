package com.micro.platform.common.log.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.platform.common.log.annotation.AuditLog;
import com.micro.platform.common.core.entity.LoginUser;
import com.micro.platform.common.core.util.SecurityUtil;
import com.micro.platform.common.log.entity.AuditLogEntity;
import com.micro.platform.common.log.handler.AuditLogEventHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 审计日志切面
 *
 * 记录敏感数据的变更历史，支持数据追踪和审计
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private AuditLogEventHandler auditLogEventHandler;

    public AuditLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(com.micro.platform.common.log.annotation.AuditLog)")
    public void auditLogPointcut() {
    }

    @Around("auditLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        AuditLogEntity auditLog = new AuditLogEntity();

        try {
            // 获取请求
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

            // 获取注解信息
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();
            AuditLog annotation = method.getAnnotation(AuditLog.class);

            // 设置日志信息
            auditLog.setModule(annotation.module());
            auditLog.setDescription(annotation.description());
            auditLog.setTableName(annotation.tableName());
            auditLog.setRequestMethod(request != null ? request.getMethod() : "");
            auditLog.setRequestUrl(request != null ? request.getRequestURI() : "");
            auditLog.setCreateTime(LocalDateTime.now());

            // 操作人信息
            LoginUser loginUser = SecurityUtil.getLoginUser();
            if (loginUser != null) {
                auditLog.setOperatorId(loginUser.getUserId());
                auditLog.setOperatorName(loginUser.getUsername());
                auditLog.setDeptId(loginUser.getDeptId());
                auditLog.setDeptName(loginUser.getDeptName());
            }

            // 请求 IP
            if (request != null) {
                auditLog.setOperIp(getIpAddr(request));
            }

            // 记录请求参数
            auditLog.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));

            // 获取操作类型（根据方法名判断）
            String methodName = method.getName();
            auditLog.setOperationType(determineOperationType(methodName));

            // 执行方法
            Object result = point.proceed();

            // 记录变更后数据
            if (annotation.recordAfter() && result != null) {
                auditLog.setAfterData(objectMapper.writeValueAsString(result));
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            auditLog.setExecuteTime(endTime - beginTime);
            auditLog.setStatus(1);

            // 保存审计日志
            saveAuditLog(auditLog);

            return result;

        } catch (Exception e) {
            auditLog.setStatus(0);
            auditLog.setErrorMsg(e.getMessage());
            auditLog.setExecuteTime(System.currentTimeMillis() - beginTime);
            saveAuditLog(auditLog);
            throw e;
        }
    }

    /**
     * 保存审计日志
     */
    private void saveAuditLog(AuditLogEntity auditLog) {
        if (auditLogEventHandler != null) {
            auditLogEventHandler.onAuditLog(auditLog);
        } else {
            // 默认实现：打印日志
            log.debug("审计日志：{}", auditLog.getDescription());
        }
    }

    /**
     * 根据方法名判断操作类型
     */
    private int determineOperationType(String methodName) {
        if (methodName.startsWith("add") || methodName.startsWith("insert") || methodName.startsWith("create") || methodName.startsWith("save")) {
            return 1; // 新增
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            return 2; // 修改
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return 3; // 删除
        } else if (methodName.startsWith("get") || methodName.startsWith("select") || methodName.startsWith("query") || methodName.startsWith("list")) {
            return 4; // 查询
        } else if (methodName.startsWith("import")) {
            return 5; // 导入
        } else if (methodName.startsWith("export")) {
            return 6; // 导出
        }
        return 7; // 其他
    }

    /**
     * 获取 IP 地址
     */
    private String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
