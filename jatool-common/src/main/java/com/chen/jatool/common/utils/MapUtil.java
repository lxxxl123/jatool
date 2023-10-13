package com.chen.jatool.common.utils;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.multi.ListValueMap;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author chenwh3
 */
public class MapUtil {

    /**
     * list根据某个字段转map
     */
    public static <T, K> Map<K, T> toMap(Collection<T> t, Function<T, K> f1) {
        return toMap(t, f1, Function.identity());
    }

    public static <T, K, V> Map<K, V> toMap(Collection<T> t, Function<T, K> f1, Function<T, V> f2) {
        if (t == null) {
            return Collections.emptyMap();
        }
        return t.stream().collect(Collectors.toMap(f1, f2, (a, b) -> a));
    }

    public static <T, K> ListValueMap<K, T> toMapList(Collection<T> t, Function<T, K> f1) {
        return toMapList(t, f1, Function.identity());
    }

    public static <T, K, V> ListValueMap<K, V> toMapList(Collection<T> t, Function<T, K> f1, Function<T, V> f2) {
        ListValueMap<K, V> list = new ListValueMap<>();
        if (!CollUtil.isEmpty(t)) {
            for (T t1 : t) {
                list.putValue(f1.apply(t1), f2.apply(t1));
            }
        }
        return list;
    }

    public static <T, K, V>  Map<K, Set<V>> toMapSet(List<T> t, Function<T, K> f1, Function<T, V> f2 , Supplier<? extends Set<V>> supplier) {
        if (CollUtil.isEmpty(t)) {
            return Collections.emptyMap();
        }
        Map<K, Set<V>> map = new HashMap<>();
        for (T o : t) {
            K k = f1.apply(o);
            V v = f2.apply(o);
            map.putIfAbsent(k, supplier.get());
            map.get(k).add(v);
        }
        return map;
    }

    /**
     * list根据两个字段转map
     */
    public static <T, K, V> MultiKeyMap<K, V> to2KeyMap(List<T> list, Function<T, K> k1, Function<T, K> k2, Function<T, V> v) {
        MultiKeyMap<K, V> map = new MultiKeyMap<>();
        for (T t : list) {
            map.put(k1.apply(t), k2.apply(t), v.apply(t));
        }
        return map;
    }

    public static <T, K, V> MultiKeyMap<K, V> to3KeyMap(List<T> list, Function<T, K> k1, Function<T, K> k2, Function<T, K> k3, Function<T, V> v) {
        MultiKeyMap<K, V> map = new MultiKeyMap<>();
        for (T t : list) {
            map.put(k1.apply(t), k2.apply(t), k3.apply(t), v.apply(t));
        }
        return map;
    }
    /**
     * list根据两个字段转map
     */
    public static <T, K, V> MultiKeyMap<K, V> to2KeyMap(List<T> list, Function<T, K> k1, Function<T, K> k2, Function<T, V> v, BinaryOperator<V> mergeFunction) {
        MultiKeyMap<K, V> map = new MultiKeyMap<>();
        for (T t : list) {
            K key1 = k1.apply(t);
            K key2 = k2.apply(t);
            V val = v.apply(t);
            if (mergeFunction != null && map.containsKey(key1, key2)) {
                map.put(key1, key2, mergeFunction.apply(map.get(key1, key2), val));
            } else {
                map.put(key1, key2, val);
            }
        }
        return map;
    }

    public static <T, K> MultiKeyMap<K, T> to2KeyMap(List<T> list, Function<T, K> k1, Function<T, K> k2) {
        return to2KeyMap(list, k1, k2, Function.identity());
    }

    public static <T, K, V> MultiKeyMap<K, List<V>> to2KeyMapList(List<T> list, Function<T, K> k1, Function<T, K> k2, Function<T, V> vFunc) {
        MultiKeyMap<K, List<V>> map = new MultiKeyMap<>();
        for (T t : list) {
            MultiKey<K> key = new MultiKey<>(k1.apply(t), k2.apply(t));
            map.computeIfAbsent(key, (k) -> new ArrayList<>());
            map.get(key).add(vFunc.apply(t));
        }
        return map;
    }


}
