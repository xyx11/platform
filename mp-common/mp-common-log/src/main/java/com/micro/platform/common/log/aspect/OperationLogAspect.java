package com.micro.platform.common.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.micro.platform.common.log.annotation.OperationLog;
import com.micro.platform.common.log.service.OperationLogService;
import com.micro.platform.common.security.util.SecurityUtil;
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
import org.springframework.web.multipart.MultipartFile;

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

    private final ObjectMapper objectMapper = new ObjectMapper()
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    @Autowired(required = false)
    private OperationLogService operationLogService;

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

            // 记录请求参数（过滤 MultipartFile 等不可序列化的对象）
            if (annotation.recordParams()) {
                operationLog.setRequestParams(paramsToString(point.getArgs()));
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

    private void saveLog(com.micro.platform.common.log.entity.OperationLog operationLog) {
        if (operationLogService != null) {
            operationLogService.saveLog(operationLog);
        } else {
            log.info("操作日志：{} - {} ({}ms)", operationLog.getStatus() == 1 ? "成功" : "失败",
                operationLog.getDescription(), operationLog.getExecuteTime());
        }
    }

    /**
     * 将参数转换为字符串（过滤 MultipartFile 等不可序列化的对象）
     */
    private String paramsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) arg;
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("fileName", file.getOriginalFilename());
                fileInfo.put("fileSize", file.getSize());
                fileInfo.put("contentType", file.getContentType());
                params.put("file[" + i + "]", fileInfo);
            } else {
                params.put("arg[" + i + "]", arg);
            }
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.warn("参数序列化失败：{}", e.getMessage());
            return "";
        }
    }
}