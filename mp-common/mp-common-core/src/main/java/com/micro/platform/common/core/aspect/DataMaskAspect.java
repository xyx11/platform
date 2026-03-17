package com.micro.platform.common.core.aspect;

import com.micro.platform.common.core.annotation.DataMask;
import com.micro.platform.common.core.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 数据脱敏切面
 * 用于对返回数据进行脱敏处理
 */
@Aspect
@Component
@Order(6)
@Slf4j
public class DataMaskAspect {

    @Pointcut("execution(* com.micro.platform..*.*(..)) && @annotation(org.springframework.web.bind.annotation.ResponseBody)")
    public void responsePointcut() {
    }

    @Around("responsePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();

        // 对返回结果进行脱敏处理
        return maskResult(result);
    }

    /**
     * 对结果进行脱敏
     */
    private Object maskResult(Object result) {
        if (result == null) {
            return null;
        }

        Class<?> clazz = result.getClass();

        // 处理集合类型
        if (result instanceof Collection) {
            Collection<?> collection = (Collection<?>) result;
            for (Object item : collection) {
                maskObject(item);
            }
            return result;
        }

        // 处理数组类型
        if (result.getClass().isArray()) {
            Object[] array = (Object[]) result;
            for (Object item : array) {
                maskObject(item);
            }
            return result;
        }

        // 处理对象
        maskObject(result);
        return result;
    }

    /**
     * 对对象进行脱敏
     */
    private void maskObject(Object obj) {
        if (obj == null || isSimpleType(obj.getClass())) {
            return;
        }

        Class<?> clazz = obj.getClass();
        List<Field> fields = getAllFields(clazz);

        for (Field field : fields) {
            DataMask dataMask = field.getAnnotation(DataMask.class);
            if (dataMask != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value instanceof String) {
                        String maskedValue = maskValue((String) value, dataMask);
                        field.set(obj, maskedValue);
                    }
                } catch (IllegalAccessException e) {
                    log.warn("无法访问字段：{}.{}", clazz.getName(), field.getName());
                }
            }
        }
    }

    /**
     * 脱敏值
     */
    private String maskValue(String value, DataMask dataMask) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return switch (dataMask.type()) {
            case PHONE -> maskPhone(value);
            case ID_CARD -> maskIdCard(value);
            case EMAIL -> maskEmail(value);
            case BANK_CARD -> maskBankCard(value);
            case ADDRESS -> maskAddress(value, dataMask);
            case NAME -> maskName(value);
            case PASSWORD -> maskPassword(value);
            case CUSTOM -> maskCustom(value, dataMask);
        };
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone.length() == 11) {
            return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        return phone;
    }

    /**
     * 身份证号脱敏
     */
    private String maskIdCard(String idCard) {
        if (idCard.length() >= 10) {
            return idCard.replaceAll("(\\d{6})\\d{8}(\\w{4})", "$1********$2");
        }
        return idCard;
    }

    /**
     * 邮箱脱敏
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            String prefix = email.substring(0, atIndex);
            String suffix = email.substring(atIndex);
            if (prefix.length() > 3) {
                return prefix.substring(0, 3) + "****" + suffix;
            } else {
                return prefix.substring(0, 1) + "****" + suffix;
            }
        }
        return email;
    }

    /**
     * 银行卡号脱敏
     */
    private String maskBankCard(String cardNo) {
        if (cardNo.length() >= 8) {
            return cardNo.replaceAll("(\\d{4})\\d+(\\d{4})", "$1 **** **** $2");
        }
        return cardNo;
    }

    /**
     * 地址脱敏
     */
    private String maskAddress(String address, DataMask dataMask) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        if (address.length() > 6) {
            return address.substring(0, 3) + "********";
        }
        return address;
    }

    /**
     * 姓名脱敏
     */
    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        } else if (name.length() == 2) {
            return name.charAt(0) + "*";
        } else {
            return name.charAt(0) + "*" + name.charAt(name.length() - 1);
        }
    }

    /**
     * 密码脱敏
     */
    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return "*".repeat(Math.min(password.length(), 10));
    }

    /**
     * 自定义脱敏
     */
    private String maskCustom(String value, DataMask dataMask) {
        if (!dataMask.showPrefixAndSuffix()) {
            return "*".repeat(Math.min(value.length(), 20));
        }

        int prefixLen = dataMask.prefixLength();
        int suffixLen = dataMask.suffixLength();

        if (value.length() <= prefixLen + suffixLen) {
            return "*".repeat(value.length());
        }

        return value.substring(0, prefixLen) +
                "*".repeat(value.length() - prefixLen - suffixLen) +
                value.substring(value.length() - suffixLen);
    }

    /**
     * 判断是否为简单类型
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                clazz == Boolean.class ||
                clazz == Character.class ||
                Number.class.isAssignableFrom(clazz) ||
                clazz == Date.class;
    }

    /**
     * 获取所有字段（包括父类）
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }
}