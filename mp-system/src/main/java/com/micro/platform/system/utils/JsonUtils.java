package com.micro.platform.system.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.*;

/**
 * JSON 处理工具类
 * 基于 Jackson ObjectMapper
 */
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 支持 Java 8 日期时间类型
        mapper.registerModule(new JavaTimeModule());
        // 禁用时间戳输出
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * 对象转格式化的 JSON 字符串
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) return null;
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) return null;
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * JSON 字符串转对象（TypeReference）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isEmpty()) return null;
        try {
            return mapper.readValue(json, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * JSON 字符串转 List
     */
    public static <T> List<T> toList(String json, Class<T> clazz) {
        return fromJson(json, new TypeReference<List<T>>() {
            @Override
            public java.lang.reflect.Type getType() {
                return new com.fasterxml.jackson.core.type.ResolvedTypeReference<List<T>>() {
                    @Override
                    public java.lang.reflect.Type getResolvedType() {
                        return new com.fasterxml.jackson.core.type.ResolvedTypeReference<List<T>>() {
                            @Override
                            public java.lang.reflect.Type getResolvedType() {
                                return null;
                            }
                        }.getResolvedType();
                    }
                }.getType();
            }
        });
    }

    /**
     * JSON 字符串转 Map
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 创建 ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * 创建 ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    /**
     * JSON 字符串转 JsonNode
     */
    public static JsonNode readTree(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * 从对象创建 JsonNode
     */
    public static JsonNode valueToTree(Object obj) {
        return mapper.valueToTree(obj);
    }

    /**
     * JsonNode 转对象
     */
    public static <T> T treeToValue(JsonNode node, Class<T> clazz) {
        if (node == null) return null;
        try {
            return mapper.treeToValue(node, clazz);
        } catch (IOException e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }

    /**
     * 获取 JSON 中的字符串字段
     */
    public static String getString(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isTextual() ? field.asText() : field.asText(null);
    }

    /**
     * 获取 JSON 中的整数字段
     */
    public static Integer getInt(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isInt() ? field.asInt() : null;
    }

    /**
     * 获取 JSON 中的长整型字段
     */
    public static Long getLong(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isLong() ? field.asLong() : null;
    }

    /**
     * 获取 JSON 中的布尔字段
     */
    public static Boolean getBoolean(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isBoolean() ? field.asBoolean() : null;
    }

    /**
     * 获取 JSON 中的双精度浮点字段
     */
    public static Double getDouble(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isNumber() ? field.asDouble() : null;
    }

    /**
     * 获取 JSON 中的数组字段
     */
    public static ArrayNode getArray(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isArray() ? (ArrayNode) field : null;
    }

    /**
     * 获取 JSON 中的对象字段
     */
    public static ObjectNode getObject(JsonNode node, String fieldName) {
        if (node == null || !node.has(fieldName)) return null;
        JsonNode field = node.get(fieldName);
        return field.isObject() ? (ObjectNode) field : null;
    }

    /**
     * 检查 JSON 是否包含字段
     */
    public static boolean hasField(JsonNode node, String fieldName) {
        return node != null && node.has(fieldName);
    }

    /**
     * 检查 JSON 是否为空对象
     */
    public static boolean isEmpty(JsonNode node) {
        return node == null || node.isMissingNode() || (node.isObject() && node.size() == 0);
    }

    /**
     * 验证字符串是否为有效的 JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) return false;
        try {
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为有效的 JSON 对象
     */
    public static boolean isJsonObject(String json) {
        if (!isValidJson(json)) return false;
        try {
            JsonNode node = mapper.readTree(json);
            return node.isObject();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为有效的 JSON 数组
     */
    public static boolean isJsonArray(String json) {
        if (!isValidJson(json)) return false;
        try {
            JsonNode node = mapper.readTree(json);
            return node.isArray();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 合并两个 JSON 对象
     */
    public static ObjectNode mergeJson(String json1, String json2) {
        try {
            ObjectNode node1 = (ObjectNode) mapper.readTree(json1);
            ObjectNode node2 = (ObjectNode) mapper.readTree(json2);
            node1.setAll(node2);
            return node1;
        } catch (IOException e) {
            throw new RuntimeException("JSON merge failed", e);
        }
    }

    /**
     * 向 JSON 数组添加元素
     */
    public static ArrayNode addToArray(String jsonArray, Object element) {
        try {
            ArrayNode array = (ArrayNode) mapper.readTree(jsonArray);
            if (element instanceof String) {
                array.add((String) element);
            } else if (element instanceof Integer) {
                array.add((Integer) element);
            } else if (element instanceof Long) {
                array.add((Long) element);
            } else if (element instanceof Double) {
                array.add((Double) element);
            } else if (element instanceof Boolean) {
                array.add((Boolean) element);
            } else {
                array.add(mapper.valueToTree(element));
            }
            return array;
        } catch (IOException e) {
            throw new RuntimeException("JSON array add failed", e);
        }
    }

    /**
     * 从 JSON 对象中移除字段
     */
    public static ObjectNode removeField(String json, String fieldName) {
        try {
            ObjectNode node = (ObjectNode) mapper.readTree(json);
            node.remove(fieldName);
            return node;
        } catch (IOException e) {
            throw new RuntimeException("JSON remove field failed", e);
        }
    }

    /**
     * 获取 JSON 数组的大小
     */
    public static int getArraySize(String jsonArray) {
        try {
            ArrayNode array = (ArrayNode) mapper.readTree(jsonArray);
            return array.size();
        } catch (IOException e) {
            throw new RuntimeException("JSON array size failed", e);
        }
    }

    /**
     * 获取 JSON 对象的字段数量
     */
    public static int getObjectSize(String json) {
        try {
            JsonNode node = mapper.readTree(json);
            return node.size();
        } catch (IOException e) {
            throw new RuntimeException("JSON object size failed", e);
        }
    }
}