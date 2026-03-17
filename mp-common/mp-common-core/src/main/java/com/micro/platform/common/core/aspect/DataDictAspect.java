package com.micro.platform.common.core.aspect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.micro.platform.common.core.annotation.Dict;
import com.micro.platform.common.core.util.DictUtils;
import lombok.Data;
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
 * 数据字典翻译切面
 * 用于对带有 @Dict 注解的字段进行字典翻译
 */
@Aspect
@Component
@Order(7)
@Slf4j
public class DataDictAspect {

    @Pointcut("execution(* com.micro.platform..*.*(..)) && @annotation(org.springframework.web.bind.annotation.ResponseBody)")
    public void responsePointcut() {
    }

    @Around("responsePointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Object result = point.proceed();

        // 对返回结果进行字典翻译处理
        return translateResult(result);
    }

    /**
     * 对结果进行字典翻译
     */
    private Object translateResult(Object result) {
        if (result == null) {
            return null;
        }

        // 处理集合类型
        if (result instanceof Collection) {
            Collection<?> collection = (Collection<?>) result;
            List<Object> translatedList = new ArrayList<>();
            for (Object item : collection) {
                translatedList.add(translateObject(item));
            }
            return translatedList;
        }

        // 处理数组类型
        if (result.getClass().isArray()) {
            Object[] array = (Object[]) result;
            Object[] translatedArray = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                translatedArray[i] = translateObject(array[i]);
            }
            return translatedArray;
        }

        // 处理对象
        return translateObject(result);
    }

    /**
     * 对对象进行字典翻译
     */
    private Object translateObject(Object obj) {
        if (obj == null || isSimpleType(obj.getClass())) {
            return obj;
        }

        Class<?> clazz = obj.getClass();

        // 如果是 Map 类型，直接返回（不处理）
        if (obj instanceof Map) {
            return obj;
        }

        // 创建新的对象并复制字段
        Object translatedObj = createInstance(clazz);
        if (translatedObj == null) {
            return obj;
        }

        List<Field> fields = getAllFields(clazz);

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                Dict dictAnnotation = field.getAnnotation(Dict.class);

                if (dictAnnotation != null && dictAnnotation.translate() && value instanceof String) {
                    // 进行字典翻译
                    String label = DictUtils.getDictLabel(dictAnnotation.type(), (String) value);
                    if (label == null || label.isEmpty()) {
                        label = dictAnnotation.defaultValue();
                    }

                    // 设置翻译后的值到新对象
                    setFieldValue(translatedObj, field, value, label);
                } else {
                    // 直接复制原值
                    setFieldValue(translatedObj, field, value, null);
                }
            } catch (IllegalAccessException e) {
                log.warn("无法访问字段：{}.{}", clazz.getName(), field.getName());
            }
        }

        return translatedObj;
    }

    /**
     * 设置字段值
     */
    private void setFieldValue(Object target, Field field, Object originalValue, String translatedLabel) {
        try {
            field.set(target, originalValue);

            // 如果有翻译后的标签，创建对应的字段存储
            if (translatedLabel != null) {
                String fieldName = field.getName();
                String labelFieldName = fieldName + "Label";

                // 尝试查找是否有对应的 Label 字段
                try {
                    Field labelField = target.getClass().getDeclaredField(labelFieldName);
                    labelField.setAccessible(true);
                    labelField.set(target, translatedLabel);
                } catch (NoSuchFieldException e) {
                    // 没有对应的 Label 字段，忽略
                }
            }
        } catch (IllegalAccessException e) {
            log.warn("无法设置字段值：{}.{}", target.getClass().getName(), field.getName());
        }
    }

    /**
     * 创建实例
     */
    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.warn("无法创建实例：{}", clazz.getName(), e);
            return null;
        }
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