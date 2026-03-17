package com.micro.platform.common.core.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典工具类
 * 用于缓存和管理字典数据
 */
public class DictUtils {

    private static final Map<String, Map<String, String>> dictCache = new ConcurrentHashMap<>();

    /**
     * 设置字典数据
     *
     * @param dictType 字典类型
     * @param dictData 字典数据（key: 字典值，value: 字典标签）
     */
    public static void setDictData(String dictType, Map<String, String> dictData) {
        if (dictType == null || dictData == null) {
            return;
        }
        dictCache.put(dictType, dictData);
    }

    /**
     * 添加字典项
     *
     * @param dictType 字典类型
     * @param value 字典值
     * @param label 字典标签
     */
    public static void addDictItem(String dictType, String value, String label) {
        if (dictType == null || value == null) {
            return;
        }
        dictCache.computeIfAbsent(dictType, k -> new HashMap<>()).put(value, label);
    }

    /**
     * 获取字典标签
     *
     * @param dictType 字典类型
     * @param value 字典值
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String value) {
        if (dictType == null || value == null) {
            return null;
        }
        Map<String, String> dictData = dictCache.get(dictType);
        if (dictData == null) {
            return null;
        }
        return dictData.get(value);
    }

    /**
     * 获取字典标签（带默认值）
     *
     * @param dictType 字典类型
     * @param value 字典值
     * @param defaultValue 默认值
     * @return 字典标签
     */
    public static String getDictLabel(String dictType, String value, String defaultValue) {
        String label = getDictLabel(dictType, value);
        return label != null ? label : defaultValue;
    }

    /**
     * 获取字典值（通过标签）
     *
     * @param dictType 字典类型
     * @param label 字典标签
     * @return 字典值
     */
    public static String getDictValue(String dictType, String label) {
        if (dictType == null || label == null) {
            return null;
        }
        Map<String, String> dictData = dictCache.get(dictType);
        if (dictData == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : dictData.entrySet()) {
            if (entry.getValue().equals(label)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 获取字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据
     */
    public static Map<String, String> getDictData(String dictType) {
        if (dictType == null) {
            return null;
        }
        return dictCache.get(dictType);
    }

    /**
     * 获取字典数据（只包含值）
     *
     * @param dictType 字典类型
     * @return 字典值列表
     */
    public static List<String> getDictValues(String dictType) {
        Map<String, String> dictData = getDictData(dictType);
        if (dictData == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(dictData.keySet());
    }

    /**
     * 获取字典标签列表
     *
     * @param dictType 字典类型
     * @return 字典标签列表
     */
    public static List<String> getDictLabels(String dictType) {
        Map<String, String> dictData = getDictData(dictType);
        if (dictData == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(dictData.values());
    }

    /**
     * 获取字典选项列表（用于下拉框）
     *
     * @param dictType 字典类型
     * @return 选项列表 [{value, label}]
     */
    public static List<Map<String, String>> getDictOptions(String dictType) {
        Map<String, String> dictData = getDictData(dictType);
        if (dictData == null) {
            return Collections.emptyList();
        }

        List<Map<String, String>> options = new ArrayList<>();
        for (Map.Entry<String, String> entry : dictData.entrySet()) {
            Map<String, String> option = new HashMap<>();
            option.put("value", entry.getKey());
            option.put("label", entry.getValue());
            options.add(option);
        }
        return options;
    }

    /**
     * 检查字典是否包含某个值
     *
     * @param dictType 字典类型
     * @param value 字典值
     * @return 是否包含
     */
    public static boolean containsValue(String dictType, String value) {
        Map<String, String> dictData = getDictData(dictType);
        if (dictData == null) {
            return false;
        }
        return dictData.containsKey(value);
    }

    /**
     * 检查字典是否包含某个标签
     *
     * @param dictType 字典类型
     * @param label 字典标签
     * @return 是否包含
     */
    public static boolean containsLabel(String dictType, String label) {
        Map<String, String> dictData = getDictData(dictType);
        if (dictData == null) {
            return false;
        }
        return dictData.containsValue(label);
    }

    /**
     * 清除字典缓存
     *
     * @param dictType 字典类型
     */
    public static void clearDict(String dictType) {
        if (dictType == null) {
            dictCache.clear();
        } else {
            dictCache.remove(dictType);
        }
    }

    /**
     * 清除所有字典缓存
     */
    public static void clearAll() {
        dictCache.clear();
    }

    /**
     * 获取所有字典类型
     *
     * @return 字典类型集合
     */
    public static Set<String> getAllDictTypes() {
        return dictCache.keySet();
    }

    /**
     * 批量设置字典数据
     *
     * @param dicts 字典数据（key: 字典类型，value: 字典数据）
     */
    public static void setDictDataBatch(Map<String, Map<String, String>> dicts) {
        if (dicts == null) {
            return;
        }
        dictCache.putAll(dicts);
    }

    /**
     * 判断字典是否为空
     *
     * @param dictType 字典类型
     * @return 是否为空
     */
    public static boolean isDictEmpty(String dictType) {
        Map<String, String> dictData = getDictData(dictType);
        return dictData == null || dictData.isEmpty();
    }

    /**
     * 获取字典大小
     *
     * @param dictType 字典类型
     * @return 字典项数量
     */
    public static int getDictSize(String dictType) {
        Map<String, String> dictData = getDictData(dictType);
        return dictData == null ? 0 : dictData.size();
    }

    /**
     * 刷新字典数据
     *
     * @param dictType 字典类型
     * @param loader 数据加载器
     */
    public static void refreshDict(String dictType, DictDataLoader loader) {
        if (dictType == null || loader == null) {
            return;
        }
        Map<String, String> data = loader.load();
        setDictData(dictType, data);
    }

    /**
     * 字典数据加载器接口
     */
    @FunctionalInterface
    public interface DictDataLoader {
        Map<String, String> load();
    }
}