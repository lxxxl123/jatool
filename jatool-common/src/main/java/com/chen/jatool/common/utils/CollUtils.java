package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.lambda.Func1;
import com.chen.jatool.common.utils.support.lambda.LambdaUtils;
import com.chen.jatool.common.utils.support.string.MessageBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

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

    /**
     *  pass 看作不做去重的条件
     */
    public static <T> Predicate<T> distinctByKeyAndSetPass(Function<? super T, ?> distinctKey, Function<? super T, Boolean> pass) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> pass.apply(t) || seen.add(distinctKey.apply(t));
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

    public static <T> Stream<T> iter2Stream(Iterable<T> obj) {
        if (obj == null) {
            return Stream.of();
        }
        return StreamSupport.stream(obj.spliterator(), false);
    }

    /**
     * 强转collection , obj 2 list<obj> , arr[] 2 list , list 2 list
     */
    public static Stream<?> toStream(Object obj) {
        if (obj == null) {
            return Stream.of();
        } else if (obj instanceof Stream) {
            return ((Stream<?>) obj);
        } else if (obj instanceof Iterable) {
            return iter2Stream((Iterable<?>) obj);
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

    @SafeVarargs
    public static <T> Stream<T> toStream(Collection<T>... list) {
        return Arrays.stream(list).filter(Objects::nonNull)
                .flatMap(Collection::stream);
    }

    @Deprecated
    public static Collection<?> toColl(Object obj) {
        return toStream(obj).collect(Collectors.toList());
    }

    public static List<?> toList(Object obj) {
        return toStream(obj).collect(Collectors.toList());
    }

    public static List<?> toNotNullList(Object obj) {
        return toStream(obj).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 尽可能不改变原来的元素，减少性能损耗
     */
    public static Collection<?> tryToColl(Object o){
        if (o == null) {
            return new ArrayList<>();
        }
        if (o instanceof Collection) {
            return (Collection<?>) o;
        }
        if (o instanceof Iterable) {
            return ListUtil.toList((Iterable<?>) o);
        }
        if (o.getClass().isArray()) {
            return Arrays.asList((Object[]) o);
        }
        return toList(o);
    }

    @Deprecated
    public static Collection<String> toStrColl(Object obj) {
        return toStrList(obj);
    }

    public static List<String> toNnStrList(Object obj) {
        return toStream(obj).map(StrUtil::toStringOrNull).collect(Collectors.toList());
    }

    public static List<String> toStrList(Object obj) {
        return toStream(obj).map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }
    public static List<String> splitToStrList(Object obj, String split){
        List<String> list = toStrList(obj);
        return list.stream().flatMap(s -> StrUtil.split(s, split).stream())
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static Set<String> splitToStrSet(Object obj, String split) {
        return new HashSet<>(splitToStrList(obj, split));
    }

    public static Set<String> toStrSet(Object obj){
        return toStream(obj).map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
    }

    public static Set<Integer> toIntSet(Object obj){
        return toStream(obj).map(Convert::toInt).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static List<String> toTrimList(Object obj) {
        return toColl(obj).stream().map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
    }

    public static List<String> toStrListFromArr(Object... obj){
        return Arrays.stream(obj).flatMap(CollUtils::toStream).map(StrUtil::toStringOrNull).filter(StringUtils::isNotBlank).collect(Collectors.toList());
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


    public static <T, R> Map<R, Long> countMap(List<T> list, Function<T, R> matcher) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }

        return list.stream()
                .map(matcher)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
    }

    public static <T, R> R maxCount(List<T> list, Function<T, R> matcher) {
        Map<R, Long> frequencyMap = countMap(list, matcher);
        if (frequencyMap.isEmpty()) {
            return null;
        }
        return frequencyMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }


    public static <T> T get(Collection<T> list, int idx) {
        int size = list.size();
        if (idx < 0) {
            idx = size + idx;
        }
        if (idx < 0 || idx >= size) {
            return null;
        }
        if (list instanceof List) {
            return ((List<T>) list).get(idx);
        }
        return IterUtil.get(list.iterator(), idx);
    }
    /**
     * 支持使用负数取倒数n个
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAny(Collection<T> list, int... idxs) {
        List<T> res = new ArrayList<>();
        List<T> list1;
        if (list instanceof List) {
            list1 = (List<T>) list;
        } else {
            list1 = (List<T>) Arrays.asList(list.toArray());
        }
        for (int idx : idxs) {
            res.add(get(list1, idx));
        }
        return res;
    }


    @SafeVarargs
    public static <T extends Comparable<T>> T min(T... list) {
        return Arrays.stream(list).filter(Objects::nonNull).min(Comparable::compareTo).orElse(null);
    }
    @SafeVarargs
    public static <T extends Comparable<T>> T max(T... list) {
        return Arrays.stream(list).filter(Objects::nonNull).max(Comparable::compareTo).orElse(null);
    }


    public static <T extends Comparable<T>, U> T max(Collection<U> list, Function<U, T> function) {
        return iter2Stream(list)
                .map(function)
                .filter(Objects::nonNull)
                .max(Comparable::compareTo).orElse(null);
    }

    public static <T extends Comparable<T>, U> T min(Iterable<U> list, Function<U, T> function) {
        return iter2Stream(list)
                .map(function)
                .filter(Objects::nonNull)
                .min(Comparable::compareTo).orElse(null);
    }

    public static < T extends Comparable<T>> T max(List<?> list, String key, Class<T> clazz) {
        return max(list, e -> Convert.convert(clazz, ObjectUtil.get(e, key), null));
    }


    public static < T extends Comparable<T>> T min(List<?> list, String key, Class<T> clazz) {
        return min(list, e -> Convert.convert(clazz, ObjectUtil.get(e, key), null));
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

    public static <T> void checkDuplicate(List<T> list, List<Func1<T, ?>> keyExtractors) {
        List<String> fieldNames = keyExtractors.stream().map(LambdaUtils::getFieldName).collect(Collectors.toList());
        checkDuplicate(list, fieldNames, keyExtractors);
    }

    public static <T> void checkDuplicate(List<T> list, List<String> fieldNames, List<? extends Function<T, ?>> keyExtractors) {
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

    /**
     * t1: 满足条件list
     * t2: 不满足条件list
     */
    public static <T> Tuple2<List<T>, List<T>> split(List<T> list, Predicate<T> predicate) {
        if (CollUtil.isEmpty(list)) {
            return Tuples.of(new ArrayList<>(), new ArrayList<>());
        }
        List<T> trueList = new ArrayList<>();
        List<T> falseList = new ArrayList<>();
        for (T t : list) {
            if (predicate.test(t)) {
                trueList.add(t);
            } else {
                falseList.add(t);
            }
        }
        return Tuples.of(trueList, falseList);
    }


    @SafeVarargs
    public static <T> List<T> ofList(T... ts) {
        return new ArrayList<>(Arrays.asList(ts));
    }

    @SafeVarargs
    public static <T> Set<T> ofSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    /**
     * 找不到则返回最小最接近的首个索引，如果abs后大于list.size()则因为所有元素均大于查找值
     */
    public static <T extends Comparable<T>> int biSearch(List<T> list, T val) {
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            T midVal = list.get(mid);
            int cmp = midVal.compareTo(val);
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid;
        }
        return -(list.size() - low +1);
    }



}
