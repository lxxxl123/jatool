package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author chenwh3
 */
public class CollUtils {

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static boolean isAnyEmpty(Collection<?>... colls) {
        return Arrays.stream(colls).anyMatch(CollUtil::isEmpty);
    }

    @SafeVarargs
    public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(Arrays.stream(keyExtractors).map(e -> e.apply(t)).collect(Collectors.toList()));
    }


    public static <T> Set<T> toSet(Collection<T> coll) {
        if (coll == null) {
            return Collections.emptySet();
        }
        if (coll instanceof Set) {
            return (Set<T>) coll;
        }
        return new HashSet<>(coll);
    }

    /**
     * 强转collection , obj 2 list<obj> , arr[] 2 list , list 2 list
     */
    public static Stream<?> toStream(Object obj) {
        if (obj == null) {
            return Stream.of();
        } else if (obj instanceof Stream) {
            return ((Stream<?>) obj);
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).stream();
        } else if (obj instanceof Iterable) {
            return StreamSupport.stream(((Iterable<?>) obj).spliterator(), false);
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            if (len == 1) {
                Object o = Array.get(obj, 0);
                if (o instanceof Collection || o instanceof Stream) {
                    return toStream(o);
                }
            }
            return Stream.of((Object [])obj);
        } else {
            return Stream.of(obj);
        }
    }

    public static Collection<?> toColl(Object obj) {
        return toStream(obj).collect(Collectors.toList());
    }

    @Deprecated
    public static Collection<String> toStrColl(Object obj) {
        return toStrList(obj);
    }
    public static List<String> toStrList(Object obj) {
        return toStream(obj).map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
    public static Set<String> toStrSet(Object obj){
        return toStream(obj).map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
    }

    public static List<String> toTrimList(Object obj) {
        return toColl(obj).stream().map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
    }

    public static List<String> toStrListFromArr(Object... obj){
        return Arrays.stream(obj).flatMap(CollUtils::toStream).map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    public static Collection<Integer> toIntColl(Object obj) {
        return toStream(obj).map(Convert::toInt).collect(Collectors.toList());
    }


    public static <T> List<List<T>> partitionList(List<T> list, int pageSize) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<List<T>> partitionedList = new ArrayList<>();
        int totalSize = list.size();
        int fromIndex = 0;
        int toIndex = pageSize;

        while (fromIndex < totalSize) {
            if (toIndex > totalSize) {
                toIndex = totalSize;
            }
            List<T> subList = CollUtil.sub(list, fromIndex, toIndex);
            partitionedList.add(subList);
            fromIndex = toIndex;
            toIndex += pageSize;
        }
        return partitionedList;
    }

    public static <T> BigDecimal sum(List<T> list, String key) {
        BigDecimal sum = BigDecimal.ZERO;
        for (T t : list) {
            Object val = ObjectUtil.get(t, key);
            if (val != null) {
                sum = sum.add(Convert.toBigDecimal(val));
            }
        }
        return sum;
    }

    public static < T extends Comparable> T max(List<?> list, String key, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(e -> Convert.convert(clazz, ObjectUtil.get(e, key), null))
                .max((a, b) -> a.compareTo(b)).get();
    }

    public static < T extends Comparable> T min(List<?> list, String key, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(e -> Convert.convert(clazz, ObjectUtil.get(e, key), null))
                .min((a, b) -> a.compareTo(b)).get();
    }


}
