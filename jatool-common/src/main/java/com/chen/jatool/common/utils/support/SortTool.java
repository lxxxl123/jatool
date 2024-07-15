package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.utils.ObjectUtil;
import org.apache.commons.collections4.comparators.FixedOrderComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 同一个key下类型不同会抛出异常。存在空值不使用nullFirst或者nullLast也会抛出异常
 * @author chenwh3
 */
public class SortTool<T> {

    public class ComparatorHolder {
        private final List<Comparator<Object>> comparators = new ArrayList<>();

        private Comparator<Object> currentComparator;

        private String currentKey;

        private Class<? extends Comparable> sortClass;

        private Object exVal;

        private Function<T,?> currentFunc;

        private ComparatorHolder() {
        }


        public void add(String key , Comparator comparator) {
            buildLastComparator();
            currentComparator = comparator;
            currentKey = key;
            sortClass = null;
            currentFunc = null;
        }

        public void add(Function<T, ?> func, Comparator comparator, Class<? extends Comparable> clazz) {
            buildLastComparator();
            currentComparator = comparator;
            currentKey = null;
            sortClass = clazz;
            currentFunc = func;
        }

        public ComparatorHolder desc(){
            currentComparator = currentComparator.reversed();
            return this;
        }

        public ComparatorHolder asc() {
            return this;
        }

        public ComparatorHolder nullLast() {
            currentComparator = Comparator.nullsLast(currentComparator);
            return this;
        }

        public ComparatorHolder nullFirst() {
            currentComparator = Comparator.nullsFirst(currentComparator);
            return this;
        }


        public ComparatorHolder sortAsNum() {
            return sortAsNum(null);
        }
        public ComparatorHolder sortAsNum(Object orElse) {
            sortClass = Numbers.class;
            exVal = orElse;
            return this;
        }


        public ComparatorHolder thenComparing(Comparator comparator) {
            currentComparator.thenComparing(comparator);
            return this;
        }

        private void buildLastComparator(){
            if (currentComparator != null) {
                final Comparator<Object> lastComparator = currentComparator;
                final String key = currentKey;
                final Class<? extends Comparable> currentClass = sortClass;
                final Object currentExVal = exVal;
                Function<Object, ? extends Comparable> sortFunc;
                Function<Object , Object> getVal ;
                if (key == null) {
                    getVal = (Function) currentFunc;
                } else {
                    getVal = e -> ObjectUtil.get(e, key);
                }
                if (currentClass != null) {
                    if (String.class.equals(currentClass)) {
                        sortFunc = e -> StrUtil.toStringOrNull(getVal.apply(e));
                    } if (Numbers.class.equals(currentClass)) {
                        sortFunc = e -> Numbers.of(getVal.apply(e), currentExVal);
                    } else {
                        sortFunc = e -> (Comparable) Convert.convert(currentClass, getVal.apply(e), currentExVal);
                    }
                } else {
                    sortFunc = e -> tryConvertComparable(getVal.apply(e));
                }

                comparators.add(Comparator.comparing(sortFunc, lastComparator));
            }
            this.sortClass = null;
            this.exVal = null;
        }

        private Comparator<Object> getFinalComparator() {
            buildLastComparator();
            if(CollUtil.isEmpty(comparators)) {
                return null;
            }
            return comparators.stream().reduce(Comparator::thenComparing).orElse(null);
        }

        public ComparatorHolder sortByKey(String key) {
            return SortTool.this.sortByKey(key);
        }
        public ComparatorHolder sortByKey(String key, Comparator comparator) {
            return SortTool.this.sortByKey(key, comparator);
        }

        public ComparatorHolder sortWithFuncAsStr(Function<T, ?> comparator) {
            return SortTool.this.sortWithFuncAsStr(comparator);
        }

        public ComparatorHolder sortWithFunc(Function<T, ?> comparator) {
            return SortTool.this.sortWithFunc(comparator);
        }


        public ComparatorHolder sortByKey(Func1<T, ?> func1) {
            return SortTool.this.sortByKey(func1);
        }

        public ComparatorHolder sortByKeyWithFixed(String key, List<String> order) {
            return SortTool.this.sortByKeyWithFixed(key, order);
        }

        public List<T> sort() {
            return SortTool.this.sort();
        }

    }

    private List<T> list;
    private ComparatorHolder comparatorHolder;

    {
        comparatorHolder = new ComparatorHolder();
    }
    public static <T> SortTool<T> of(List<T> list) {
        return new SortTool<>(list);
    }

    private SortTool(List<T> list) {
        this.list = list;
    }


    private static Comparable<Object> tryConvertComparable(Object object) {
        if (object instanceof Comparable) {
            return (Comparable<Object>) object;
        } else {
            return (Comparable) Convert.toStr(object);
        }
    }

    public ComparatorHolder sortByKey(Func1<T, ?> func1) {
        comparatorHolder.add(LambdaUtil.getFieldName(func1), Comparator.naturalOrder());
        return comparatorHolder;
    }

    public ComparatorHolder sortWithFuncAsStr(Function<T, ?> func1){
        comparatorHolder.add(func1, Comparator.naturalOrder(), String.class);
        return comparatorHolder;
    }

    public ComparatorHolder sortWithFunc(Function<T, ?> func1){
        comparatorHolder.add(func1, Comparator.naturalOrder(), null);
        return comparatorHolder;
    }

    public ComparatorHolder sortByKey(String key) {
        comparatorHolder.add(key, Comparator.naturalOrder());
        return comparatorHolder;
    }

    public ComparatorHolder sortByKey(String key, Comparator comparator) {
        comparatorHolder.add(key, comparator);
        return comparatorHolder;
    }

    public ComparatorHolder sortByKeyWithFixed(String key, List<String> order) {
        FixedOrderComparator<Object> fixedOrder = new FixedOrderComparator<>(order.toArray());
        fixedOrder.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
        comparatorHolder.add(key, fixedOrder);
        return comparatorHolder;
    }


    private void clear(){
        list = null;
        comparatorHolder = null;
    }

    public List<T> sort() {
        Comparator<Object> comparator = comparatorHolder.getFinalComparator();
        if (comparator == null || CollUtil.isEmpty(list)) {
            return list;
        }
        try {
            return list.stream().sorted(comparator).collect(Collectors.toList());
        } finally {
            clear();
        }
    }


    /*测试代码*/
    public static void main(String[] args) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(Maps.ofArr("a", i, "b", "b" + (10 - i)).getMap(String.class, Object.class));
            list.add(Maps.ofArr("a", "100" + i, "b", "b" + (10 - i - 2)).getMap(String.class, Object.class));
            list.add(Maps.ofArr("a", "-" + i, "b", "b" + (10 - i - 2)).getMap(String.class, Object.class));
            list.add(Maps.ofArr("a", null, "b", "b" + (10 - i - 2)).getMap(String.class, Object.class));
        }

        list = SortTool.of(list)
                .sortWithFuncAsStr(e -> e.get("b").toString().substring(1)).sortAsNum()
                .sortByKey("a").sortAsNum().nullLast().desc()
                .sortByKey("b")
                .sort();
        list.forEach(e -> {
            System.out.println(e);
        });

        System.out.println(SortTool.of(ListUtil.of("a1", "c5", "d2"))
                .sortWithFuncAsStr(e -> e.substring(1))
                .sort()
        );

    }



}
