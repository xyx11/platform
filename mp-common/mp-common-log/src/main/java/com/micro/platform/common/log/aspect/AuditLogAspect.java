package com.micro.platform.common.log.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.platform.common.log.annotation.AuditLog;
import com.micro.platform.common.security.entity.LoginUser;
import com.micro.platform.common.security.util.SecurityUtil;
import com.micro.platform.system.entity.SysAuditLog;
import com.micro.platform.system.service.SysAuditLogService;
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
    private final SysAuditLogService auditLogService;

    public AuditLogAspect(ObjectMapper objectMapper, SysAuditLogService auditLogService) {
        this.objectMapper = objectMapper;
        this.auditLogService = auditLogService;
    }

    @Pointcut("@annotation(com.micro.platform.common.log.annotation.AuditLog)")
    public void auditLogPointcut() {
    }

    @Around("auditLogPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        SysAuditLog auditLog = new SysAuditLog();

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

                // 如果是修改操作，记录变更字段
                if (auditLog.getOperationType() == 2) {
                    auditLog.setChangeFields(compareChanges(point.getArgs(), result, annotation.ignoreFields()));
                }
            }

            // 执行时长
            long executeTime = System.currentTimeMillis() - beginTime;
            auditLog.setExecuteTime(executeTime);
            auditLog.setStatus(1);

            // 保存日志（异步）
            saveLog(auditLog);

            return result;
        } catch (Exception e) {
            auditLog.setStatus(0);
            auditLog.setErrorMsg(e.getMessage());
            auditLog.setExecuteTime(System.currentTimeMillis() - beginTime);
            saveLog(auditLog);
            throw e;
        }
    }

    /**
     * 根据方法名判断操作类型
     * 1-新增 2-修改 3-删除 4-查询 5-导入 6-导出 7-其他
     */
    private int determineOperationType(String methodName) {
        if (methodName.startsWith("save") || methodName.startsWith("insert") || methodName.startsWith("add") || methodName.startsWith("create")) {
            return 1; // 新增
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || methodName.startsWith("modify")) {
            return 2; // 修改
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove") || methodName.startsWith("remove")) {
            return 3; // 删除
        } else if (methodName.startsWith("get") || methodName.startsWith("select") || methodName.startsWith("query") || methodName.startsWith("find") || methodName.startsWith("list")) {
            return 4; // 查询
        } else if (methodName.startsWith("import")) {
            return 5; // 导入
        } else if (methodName.startsWith("export")) {
            return 6; // 导出
        }
        return 7; // 其他
    }

    /**
     * 比较变更前后的数据，返回变更字段
     */
    private String compareChanges(Object[] args, Object result, String[] ignoreFields) throws JsonProcessingException {
        if (args == null || args.length == 0 || result == null) {
            return null;
        }

        // 获取第一个参数作为变更前数据
        Object beforeObj = args[0];
        if (beforeObj == null) {
            return null;
        }

        Map<String, Object> beforeMap = objectMapper.convertValue(beforeObj, Map.class);
        Map<String, Object> afterMap = objectMapper.convertValue(result, Map.class);

        Map<String, Object> changeMap = new HashMap<>();
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreFields));

        // 比较字段变化
        for (Map.Entry<String, Object> entry : afterMap.entrySet()) {
            String key = entry.getKey();
            if (ignoreSet.contains(key)) {
                continue;
            }

            Object beforeValue = beforeMap.get(key);
            Object afterValue = entry.getValue();

            if (!Objects.equals(beforeValue, afterValue)) {
                Map<String, Object> change = new HashMap<>();
                change.put("before", beforeValue);
                change.put("after", afterValue);
                changeMap.put(key, change);
            }
        }

        if (changeMap.isEmpty()) {
            return null;
        }

        return objectMapper.writeValueAsString(changeMap);
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
    private void saveLog(SysAuditLog auditLog) {
        try {
            // 异步保存到数据库
            // 实际项目中可以使用 Spring 的 @Async 或者消息队列
            auditLogService.save(auditLog);
            log.info("审计日志保存成功：{}", auditLog.getDescription());
        } catch (Exception e) {
            log.error("审计日志保存失败", e);
        }
    }
}