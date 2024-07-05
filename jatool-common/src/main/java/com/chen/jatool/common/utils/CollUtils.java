package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.string.MessageBuilder;
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

    public static <T> Predicate<T> repeatByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        Set<Object> existed = ConcurrentHashMap.newKeySet();
        return t -> {
            Object val = keyExtractor.apply(t);
            return !seen.add(val) && existed.add(val);
        };
    }

    public static boolean isAnyEmpty(Collection<?>... colls) {
        return Arrays.stream(colls).anyMatch(CollUtil::isEmpty);
    }

    @SafeVarargs
    public static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(Arrays.stream(keyExtractors).map(e -> e.apply(t)).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static <T> Predicate<T> repeatByKeys(Function<? super T, ?>... keyExtractors) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        Set<Object> existed = ConcurrentHashMap.newKeySet();
        return t -> {
            List<?> val = Arrays.stream(keyExtractors).map(e -> e.apply(t)).collect(Collectors.toList());
            return !seen.add(val) && existed.add(val);
        };
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

    @Deprecated
    public static Collection<?> toColl(Object obj) {
        return toStream(obj).collect(Collectors.toList());
    }

    public static List<?> toList(Object obj) {
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

    @Deprecated
    public static Collection<Integer> toIntColl(Object obj) {
        return toStream(obj).map(Convert::toInt).collect(Collectors.toList());
    }

    public static List<Integer> toIntList(Object obj) {
        return toStream(obj).map(Convert::toInt).filter(Objects::nonNull).collect(Collectors.toList());
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

    public static boolean containsAny(Collection<?> coll1, Collection<?> coll2){
        if (CollUtil.isEmpty(coll1) || CollUtil.isEmpty(coll2)) {
            return false;
        }
        Collection<?> doFor;
        Collection<?> doContains;
        if (coll1 instanceof Set && !(coll2 instanceof Set)) {
            doContains = coll1;
            doFor = coll2;
        } else if (coll2 instanceof Set && !(coll1 instanceof Set)) {
            doContains = coll2;
            doFor = coll1;
        } else if (coll1.size() < coll2.size()) {
            doContains = coll2;
            doFor = coll1;
        } else {
            doContains = coll1;
            doFor = coll2;
        }
        for (Object o : doFor) {
            if (doContains.contains(o)) {
                return true;
            }
        }
        return false;
    }


    public static <T> void checkDuplicate(List<T> list, List<String> fieldNames, List<Function<T, ?>> keyExtractors) {
        MessageBuilder mb = new MessageBuilder("数据重复：</br>", "</br>", "");
        list.stream().filter(CollUtils.repeatByKeys(keyExtractors.toArray(new Function[]{})))
                .forEach(e -> {
                    mb.append("[ ");
                    for (int i = 0; i < keyExtractors.size(); i++) {
                        //必须强转，删除后会编译失败
                        Object apply = keyExtractors.get(i).apply((T) e);
                        String field = CollUtil.get(fieldNames, i);
                        if (field != null) {
                            mb.append("{}={}", CollUtil.get(fieldNames, i), apply);
                        } else {
                            mb.append("{}", CollUtil.get(fieldNames, i), apply);
                        }
                        if (i != fieldNames.size() - 1) mb.append("，");
                    }
                    mb.append(" ]");
                });
        if (mb.containMsg()) {
            throw new ServiceException(mb.toString());
        }
    }


}
