package com.chen.jatool.common.utils.support;

import java.util.*;
import java.util.function.*;

/**
 * @author chenwh3
 */
public class Collects {

    public static <T, T1, T2, R2, A extends Map<T1, List<T2>>, R extends Map<T1, R2>> SimCollector<T, A, R> groupBy(
            Function<T, T1> getKey, Function<T, T2> getValue, Supplier<A> supplier, BiFunction<T1, List<T2>, R2> fin) {
        BiConsumer<A, T> accumulator = (a, t) -> {
            Object key = getKey.apply(t);
            Object value = getValue.apply(t);
            if (key != null && value != null) {
                List<T2> list = a.get(key);
                if (list == null) {
                    list = new ArrayList<>();
                    a.put((T1) key, list);
                }
                list.add((T2) value);
            }
        };

        BinaryOperator<A> combiner = (a, b) -> {
            throw new UnsupportedOperationException("combiner");
        };

        Function<A, R> finisher = (a) -> {
            HashMap<T1, R2> res = new HashMap<>();
            for (Map.Entry<T1, List<T2>> entry : a.entrySet()) {
                R2 v2 = fin.apply(entry.getKey(), entry.getValue());
                res.put(entry.getKey(), v2);
            }
            return (R) res;
        };

        return new SimCollector<>(supplier, accumulator, combiner, finisher, Collections.emptySet());
    }

    public static <T, T1, R2, A extends Map<T1, List<T>>, R extends Map<T1, R2>> SimCollector<T, A, R> groupBy(
            Function<T, T1> getKey, BiFunction<T1, List<T>, R2> fin) {
        return groupBy(getKey, Function.identity(), () -> (A) new HashMap<T1, List<T>>(), fin);
    }

    public static <T, T1, R2, A extends Map<T1, List<T>>, R extends Map<T1, R2>> SimCollector<T, A, R> orderWithGroup(
            Function<T, T1> getKey, BiFunction<T1, List<T>, R2> fin) {
        return groupBy(getKey, Function.identity(), () -> (A) new HashMap<T1, List<T>>(), fin);
    }


}
