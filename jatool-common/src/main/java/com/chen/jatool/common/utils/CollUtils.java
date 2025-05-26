package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.string.MessageBuilder;
import org.apache.commons.lang3.StringUtils;
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

    public static <T> Stream<T> toStream(List<T>... list) {
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

    /**
     * 支持使用负数取倒数n个
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAny(Collection<T> list, int... idxs) {
        List<T> res = new ArrayList<>();
        int size = list.size();

        List<T> list1 ;
        if (list instanceof List) {
            list1 = (List<T>) list;
        } else {
            list1 = (List<T>) Arrays.asList(list.toArray());
        }
        for (int idx : idxs) {
            if (idx < 0) {
                idx = Math.floorMod(idx, size);
            }
            if (idx < size) {
                res.add(list1.get(idx));
            }
        }
        return res;
    }

    public static < T extends Comparable> T max(List<?> list, String key, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(e -> Convert.convert(clazz, ObjectUtil.get(e, key), null))
                .max((a, b) -> a.compareTo(b)).get();
    }

    public static <T extends Comparable, U> T max(List<U> list, Function<U, T> function) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(e -> function.apply(e))
                .filter(e -> e != null)
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

    public static <T> void checkDuplicate(List<T> list, List<Func1<T, ?>> keyExtractors) {
        List<String> fieldNames = keyExtractors.stream().map(LambdaUtil::getFieldName).collect(Collectors.toList());
        List<Function<T, ?>> keysList = keyExtractors.stream().map(e -> {
            Function<T, ?> r = t -> {
                try {
                    return e.call(t);
                } catch (Exception ex) {
                    throw new ServiceException(ex);
                }
            };
            return r;
        }).collect(Collectors.toList());

        checkDuplicate(list, fieldNames, keysList);
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


    public static <T> List<T> ofList(T... ts) {
        List<T> list = new ArrayList<>();
        for (T t : ts) {
            list.add(t);
        }
        return list;
    }

    public static <T> Set<T> ofSet(T... ts) {
        Set<T> set = new HashSet<T>();
        for (T t : ts) {
            set.add(t);
        }
        return set;
    }


}
