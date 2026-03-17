package com.micro.platform.common.core.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集合工具类
 */
public class CollectionUtils {

    /**
     * 检查集合是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 检查集合是否不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    /**
     * 检查 Map 是否为空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 检查 Map 是否不为空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 数组转 List
     */
    public static <T> List<T> toArray(T... items) {
        return items != null ? Arrays.asList(items) : new ArrayList<>();
    }

    /**
     * List 转 Set
     */
    public static <T> Set<T> toSet(Collection<T> collection) {
        if (isEmpty(collection)) return new HashSet<>();
        return new HashSet<>(collection);
    }

    /**
     * 获取 List 的第一个元素
     */
    public static <T> T first(List<T> list) {
        if (isEmpty(list)) return null;
        return list.get(0);
    }

    /**
     * 获取 List 的最后一个元素
     */
    public static <T> T last(List<T> list) {
        if (isEmpty(list)) return null;
        return list.get(list.size() - 1);
    }

    /**
     * 获取 List 的指定范围
     */
    public static <T> List<T> subList(List<T> list, int fromIndex, int toIndex) {
        if (isEmpty(list)) return new ArrayList<>();
        if (fromIndex >= list.size()) return new ArrayList<>();
        if (toIndex > list.size()) toIndex = list.size();
        return list.subList(fromIndex, toIndex);
    }

    /**
     * 获取 List 的前 N 个元素
     */
    public static <T> List<T> limit(List<T> list, int n) {
        return subList(list, 0, n);
    }

    /**
     * 获取 List 的跳过前 N 个元素
     */
    public static <T> List<T> skip(List<T> list, int n) {
        if (isEmpty(list) || n >= list.size()) return new ArrayList<>();
        return subList(list, n, list.size());
    }

    /**
     * List 去重（基于某个字段）
     */
    public static <T> List<T> distinctBy(Collection<T> collection, Function<? super T, ?> keyExtractor) {
        if (isEmpty(collection)) return new ArrayList<>();
        return collection.stream()
                .filter(distinctByKey(keyExtractor))
                .collect(Collectors.toList());
    }

    /**
     * 去重 Predicate
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * 将 List 按指定大小分块
     */
    public static <T> List<List<T>> chunk(Collection<T> collection, int size) {
        if (isEmpty(collection) || size <= 0) return new ArrayList<>();
        List<List<T>> result = new ArrayList<>();
        List<T> current = new ArrayList<>(size);
        int count = 0;
        for (T item : collection) {
            current.add(item);
            count++;
            if (count == size) {
                result.add(current);
                current = new ArrayList<>(size);
                count = 0;
            }
        }
        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    /**
     * 合并多个 Collection
     */
    @SafeVarargs
    public static <T> List<T> concat(Collection<T>... collections) {
        List<T> result = new ArrayList<>();
        if (collections != null) {
            for (Collection<T> collection : collections) {
                if (isNotEmpty(collection)) {
                    result.addAll(collection);
                }
            }
        }
        return result;
    }

    /**
     * 获取 Collection 大小（null 返回 0）
     */
    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /**
     * 获取 Map 大小（null 返回 0）
     */
    public static int size(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    /**
     * 安全获取 List 元素
     */
    public static <T> T get(List<T> list, int index) {
        if (isEmpty(list) || index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    /**
     * 安全获取 Map 值
     */
    public static <K, V> V get(Map<K, V> map, K key) {
        return get(map, key, null);
    }

    /**
     * 安全获取 Map 值（带默认值）
     */
    public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
        if (isEmpty(map)) return defaultValue;
        V value = map.get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 安全获取 Map 值（第一个）
     */
    public static <K, V> V getFirst(Map<K, V> map) {
        if (isEmpty(map)) return null;
        return map.values().iterator().next();
    }

    /**
     * 检查 Collection 是否包含元素
     */
    public static boolean contains(Collection<?> collection, Object element) {
        if (isEmpty(collection)) return false;
        return collection.contains(element);
    }

    /**
     * 检查 Collection 是否包含任意指定元素
     */
    public static boolean containsAny(Collection<?> collection, Object... elements) {
        if (isEmpty(collection) || elements == null) return false;
        for (Object element : elements) {
            if (collection.contains(element)) return true;
        }
        return false;
    }

    /**
     * 检查 Collection 是否包含所有指定元素
     */
    public static boolean containsAll(Collection<?> collection, Object... elements) {
        if (isEmpty(collection) || elements == null) return false;
        for (Object element : elements) {
            if (!collection.contains(element)) return false;
        }
        return true;
    }

    /**
     * 反转 List
     */
    public static <T> List<T> reverse(List<T> list) {
        if (isEmpty(list)) return new ArrayList<>();
        List<T> result = new ArrayList<>(list);
        Collections.reverse(result);
        return result;
    }

    /**
     * 打乱 List 顺序
     */
    public static <T> List<T> shuffle(List<T> list) {
        if (isEmpty(list)) return new ArrayList<>();
        List<T> result = new ArrayList<>(list);
        Collections.shuffle(result);
        return result;
    }

    /**
     * 排序 List
     */
    public static <T extends Comparable<? super T>> List<T> sort(Collection<T> collection) {
        if (isEmpty(collection)) return new ArrayList<>();
        List<T> result = new ArrayList<>(collection);
        Collections.sort(result);
        return result;
    }

    /**
     * 降序排序 List
     */
    public static <T extends Comparable<? super T>> List<T> sortDescending(Collection<T> collection) {
        if (isEmpty(collection)) return new ArrayList<>();
        List<T> result = sort(collection);
        Collections.reverse(result);
        return result;
    }

    /**
     * 获取最小值
     */
    public static <T extends Comparable<? super T>> T min(Collection<T> collection) {
        if (isEmpty(collection)) return null;
        T min = null;
        for (T item : collection) {
            if (min == null || item.compareTo(min) < 0) {
                min = item;
            }
        }
        return min;
    }

    /**
     * 获取最大值
     */
    public static <T extends Comparable<? super T>> T max(Collection<T> collection) {
        if (isEmpty(collection)) return null;
        T max = null;
        for (T item : collection) {
            if (max == null || item.compareTo(max) > 0) {
                max = item;
            }
        }
        return max;
    }

    /**
     * 求和（Integer）
     */
    public static int sumInt(Collection<Integer> collection) {
        if (isEmpty(collection)) return 0;
        return collection.stream().mapToInt(i -> i != null ? i : 0).sum();
    }

    /**
     * 求和（Long）
     */
    public static long sumLong(Collection<Long> collection) {
        if (isEmpty(collection)) return 0L;
        return collection.stream().mapToLong(l -> l != null ? l : 0L).sum();
    }

    /**
     * 求和（Double）
     */
    public static double sumDouble(Collection<Double> collection) {
        if (isEmpty(collection)) return 0.0;
        return collection.stream().mapToDouble(d -> d != null ? d : 0.0).sum();
    }

    /**
     * 计算平均值（Integer）
     */
    public static double averageInt(Collection<Integer> collection) {
        if (isEmpty(collection)) return 0.0;
        return collection.stream().mapToInt(i -> i != null ? i : 0).average().orElse(0.0);
    }

    /**
     * 计算平均值（Long）
     */
    public static double averageLong(Collection<Long> collection) {
        if (isEmpty(collection)) return 0.0;
        return collection.stream().mapToLong(l -> l != null ? l : 0L).average().orElse(0.0);
    }

    /**
     * 计算平均值（Double）
     */
    public static double averageDouble(Collection<Double> collection) {
        if (isEmpty(collection)) return 0.0;
        return collection.stream().mapToDouble(d -> d != null ? d : 0.0).average().orElse(0.0);
    }

    /**
     * 交集
     */
    public static <T> List<T> intersect(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1) || isEmpty(collection2)) return new ArrayList<>();
        return collection1.stream()
                .filter(collection2::contains)
                .collect(Collectors.toList());
    }

    /**
     * 差集（collection1 中有但 collection2 中没有的）
     */
    public static <T> List<T> difference(Collection<T> collection1, Collection<T> collection2) {
        if (isEmpty(collection1)) return new ArrayList<>();
        if (isEmpty(collection2)) return new ArrayList<>(collection1);
        return collection1.stream()
                .filter(item -> !collection2.contains(item))
                .collect(Collectors.toList());
    }

    /**
     * 并集（去重）
     */
    public static <T> List<T> union(Collection<T> collection1, Collection<T> collection2) {
        Set<T> set = new HashSet<>();
        if (isNotEmpty(collection1)) set.addAll(collection1);
        if (isNotEmpty(collection2)) set.addAll(collection2);
        return new ArrayList<>(set);
    }

    /**
     * 判断两个集合是否有交集
     */
    public static boolean hasIntersection(Collection<?> collection1, Collection<?> collection2) {
        if (isEmpty(collection1) || isEmpty(collection2)) return false;
        // 优化：遍历较小的集合
        if (collection1.size() > collection2.size()) {
            Collection<?> temp = collection1;
            collection1 = collection2;
            collection2 = temp;
        }
        for (Object item : collection1) {
            if (collection2.contains(item)) return true;
        }
        return false;
    }

    /**
     * 创建不可变 List
     */
    @SafeVarargs
    public static <T> List<T> of(T... items) {
        return Collections.unmodifiableList(Arrays.asList(items));
    }

    /**
     * 创建空 List
     */
    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    /**
     * 创建空 Set
     */
    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    /**
     * 创建空 Map
     */
    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    /**
     * 同步修改 List
     */
    public static <T> List<T> synchronizedList(List<T> list) {
        return Collections.synchronizedList(list != null ? list : new ArrayList<>());
    }

    /**
     * 单元素 List
     */
    public static <T> List<T> singletonList(T item) {
        return item != null ? Collections.singletonList(item) : Collections.emptyList();
    }

    /**
     * 单元素 Set
     */
    public static <T> Set<T> singletonSet(T item) {
        return item != null ? Collections.singleton(item) : Collections.emptySet();
    }
}