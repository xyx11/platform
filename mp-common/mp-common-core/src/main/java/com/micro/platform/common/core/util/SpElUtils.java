package com.micro.platform.common.core.util;

import com.micro.platform.common.core.annotation.OperationLog;
import com.micro.platform.common.core.annotation.OperationType;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * SpEL 表达式工具类
 * 用于解析注解中的 SpEL 表达式
 */
public class SpElUtils {

    private static final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 解析 SpEL 表达式
     *
     * @param spelExpression SpEL 表达式
     * @param method 方法
     * @param args 方法参数
     * @return 解析结果
     */
    public static Object parseExpression(String spelExpression, Method method, Object[] args) {
        String[] params = discoverParameterNames(method);
        Map<String, Object> context = buildContext(params, args);

        StandardEvaluationContext contextObj = new StandardEvaluationContext();
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            contextObj.setVariable(entry.getKey(), entry.getValue());
        }

        Expression expression = parser.parseExpression(spelExpression);
        return expression.getValue(contextObj);
    }

    /**
     * 构建上下文
     */
    private static Map<String, Object> buildContext(String[] params, Object[] args) {
        Map<String, Object> context = new HashMap<>();

        // 添加方法参数
        for (int i = 0; i < params.length; i++) {
            context.put(params[i], args[i]);
        }

        // 添加请求相关信息
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            context.put("request", request);
            context.put("remoteAddr", request.getRemoteAddr());
            context.put("sessionId", request.getSession(false));
        }

        return context;
    }

    /**
     * 获取操作类型描述
     */
    public static String getOperationTypeDescription(OperationType type) {
        if (type == null) return "其他";
        return type.getDescription();
    }

    /**
     * 从注解中获取操作日志信息
     */
    public static Map<String, Object> getOperationLogInfo(OperationLog logAnnotation,
                                                           Method method,
                                                           Object[] args) {
        Map<String, Object> info = new HashMap<>();

        info.put("module", logAnnotation.module());
        info.put("type", logAnnotation.type().name());
        info.put("typeDescription", getOperationTypeDescription(logAnnotation.type()));
        info.put("description", parseValue(logAnnotation.description(), method, args));

        return info;
    }

    /**
     * 解析值（支持 SpEL）
     */
    private static String parseValue(String value, Method method, Object[] args) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // 检查是否是 SpEL 表达式
        if (value.contains("#") || value.contains("[")) {
            try {
                Object result = parseExpression(value, method, args);
                return result != null ? result.toString() : "";
            } catch (Exception e) {
                return value;
            }
        }

        return value;
    }

    /**
     * 发现参数名称
     */
    private static String[] discoverParameterNames(Method method) {
        String[] paramNames = new String[method.getParameterCount()];
        for (int i = 0; i < method.getParameterCount(); i++) {
            paramNames[i] = "arg" + i;
        }
        return paramNames;
    }

    /**
     * 获取当前请求的 IP 地址
     */
    public static String getCurrentIp() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "unknown";
    }

    /**
     * 获取当前请求的 URI
     */
    public static String getCurrentUri() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                return request.getRequestURI();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "";
    }

    /**
     * 获取当前用户 ID（假设用户 ID 存储在请求属性中）
     */
    public static Long getCurrentUserId() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                Object userId = request.getAttribute("userId");
                if (userId != null) {
                    return Long.valueOf(userId.toString());
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
                return request.getHeader("X-Username");
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
}