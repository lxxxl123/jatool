package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author chenwh3
 */
public class CollUtils {

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @SafeVarargs
    public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(Arrays.stream(keyExtractors).map(e -> e.apply(t)).collect(Collectors.toList()));
    }


    public static <T> Set<T> toSet(Collection<T> coll){
        if (coll == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(coll);
    }
    /**
     * 强转collection , obj 2 list<obj> , arr[] 2 list , list 2 list
     */
    public static Collection<?> toColl(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        } else if (obj instanceof Collection) {
            return (Collection<?>) obj;
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            if (len == 1 && Array.get(obj, 0) instanceof Collection) {
                return (Collection<?>) Array.get(obj, 0);
            }
            List<Object> list = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                list.add(Array.get(obj, i));
            }
            return list;
        } else {
            return Collections.singletonList(obj);
        }
    }

    public static Collection<String> toStrColl(Object obj) {
        return toColl(obj).stream().map(StringUtil::toNotNullStr).filter(StrUtil::isNotBlank).collect(Collectors.toList());
    }
    public static Collection<Integer> toIntColl(Object obj) {
        return toColl(obj).stream().map(Convert::toInt).collect(Collectors.toList());
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

    public static <T> List<List<T>> partitionList(List<T> list, int pageSize) {
        if(list == null || list.isEmpty()) {
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

    public static void main(String[] args) {
        for (List<Integer> list : partitionList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 4)) {
            list.add(1000);
            System.out.println(list);
        }
    }

}
