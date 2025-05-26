package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import com.chen.jatool.common.utils.ConvertUtil;
import com.chen.jatool.common.utils.ObjectUtil;
import com.chen.jatool.common.utils.support.lambda.Func1;
import org.apache.commons.collections4.comparators.FixedOrderComparator;

import java.util.*;
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

        private Function<T, Comparable<Object>> func;


        private ComparatorHolder() {
        }

        protected  <S> void add(final Func1<T, ?> func1, final Class<S> clazz, final S orElse, Comparator comparator) {
            if (comparator == null) {
                comparator = Comparator.naturalOrder();
            }
            currentComparator = comparator;
            func = t -> {
                if (clazz == null) {
                    return tryConvertComparable(func1.apply(t));
                }
                return tryConvertComparable(ConvertUtil.convert(func1.apply(t), clazz, orElse));
            };
        }

        public ComparatorHolder desc(){
            currentComparator = currentComparator.reversed();
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


        public ComparatorHolder thenComparing(Comparator comparator) {
            currentComparator.thenComparing(comparator);
            return this;
        }

        public SortTool<T> next() {
            buildLastComparator();
            return SortTool.this;
        }

        private void buildLastComparator(){
            comparators.add(Comparator.comparing((Function) func, currentComparator));
        }

        private Comparator<Object> getFinalComparator() {
            buildLastComparator();
            if(CollUtil.isEmpty(comparators)) {
                return null;
            }
            return comparators.stream().reduce(Comparator::thenComparing).orElse(null);
        }

        public List<T> sort() {
            return SortTool.this.sort();
        }
        public List<T> sorted() {
            return SortTool.this.sorted();
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

    public static <T> SortTool<T> of(Collection<T> list) {
        return new SortTool<>(new ArrayList<>(list));
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

    public <S> ComparatorHolder sortByKey(Func1<T, ?> func1, Class<S> clazz, S orElse, Comparator comparator) {
        comparatorHolder.add(func1, clazz, orElse, comparator);
        return comparatorHolder;
    }

    public <S> ComparatorHolder sortByKey(String key, Class<S> clazz, S orElse, Comparator comparator) {
        return sortByKey(e -> ObjectUtil.get(e, key), clazz, orElse, comparator);
    }

    public ComparatorHolder sortByKey(Func1<T, ?> func1) {
        return sortByKey(func1,null,null,null);
    }


    public ComparatorHolder sortByKey(String key) {
        return sortByKey(key,null,null,null);
    }

    public ComparatorHolder sortAsStr(String key) {
        return sortByKey(key, String.class, null, null);
    }

    public ComparatorHolder sortAsStr(Func1<T,?> func1) {
        return sortByKey(func1, String.class, null, null);
    }

    public ComparatorHolder sortAsInt(String key) {
        return sortByKey(key, Integer.class, null, null);
    }

    public ComparatorHolder sortAsInt(Func1<T, ?> func1) {
        return sortByKey(func1, Integer.class, null, null);
    }

    public ComparatorHolder sortAsNum(Func1<T, ?> func1) {
        return sortByKey(func1, Numbers.class, null, null);
    }

    public ComparatorHolder sortAsNum(String key) {
        return sortByKey(key, Numbers.class, null, null);
    }

    public ComparatorHolder sortAsTimes(String key) {
        return sortByKey(key, DateTimes.class, null, null);
    }

    public ComparatorHolder sortAsTimes(Func1<T, ?> func1) {
        return sortByKey(func1, DateTimes.class, null, null);
    }

    public ComparatorHolder sortAsTimes(String key, Object orElse) {
        return sortByKey(key, DateTimes.class, DateTimes.of(orElse), null);
    }

    public ComparatorHolder sortAsTimes(Func1<T, ?> func1, Object orElse) {
        return sortByKey(func1, DateTimes.class, DateTimes.of(orElse), null);
    }


    public ComparatorHolder sortByKeyWithFixed(String key, List<?> order) {
        FixedOrderComparator<Object> fixedOrder = new FixedOrderComparator<>(order.toArray());
        fixedOrder.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
        return sortByKey(key, null, null, fixedOrder);
    }

    public ComparatorHolder sortByKeyWithFixed(Func1<T, ?> func1, List<?> order) {
        FixedOrderComparator<Object> fixedOrder = new FixedOrderComparator<>(order.toArray());
        fixedOrder.setUnknownObjectBehavior(FixedOrderComparator.UnknownObjectBehavior.AFTER);
        return sortByKey(func1, null, null, fixedOrder);
    }


    public List<T> sort() {
        Comparator<Object> comparator = comparatorHolder.getFinalComparator();
        if (comparator == null || CollUtil.isEmpty(list)) {
            return list;
        }
        return list.stream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 在原有列表排序
     */
    public List<T> sorted(){
        Comparator<Object> comparator = comparatorHolder.getFinalComparator();
        if (comparator == null || CollUtil.isEmpty(list)) {
            return list;
        }
        list.sort(comparator);
        return list;
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
                .sortAsNum(e -> e.get("b").toString().substring(1))
                .next().sortAsNum("a").nullLast().desc()
                .next().sortByKey("b")
                .sort();
        list.forEach(e -> {
            System.out.println(e);
        });

        System.out.println(SortTool.of(ListUtil.of("a1", "c5", "d2"))
                .sortByKey(e -> e.substring(1))
                .sort()
        );

        System.out.println(SortTool.of(ListUtil.of("a51", "c52", "d23", "x25", null))
                .sortByKeyWithFixed(e -> e == null ? null : e.charAt(1), ListUtil.of("5", "1", "2")).nullFirst()
                .next().sortByKeyWithFixed(e -> e.charAt(2), ListUtil.of("5", "1", "2")).desc()
                .sort()
        );

        System.out.println(SortTool.of(ListUtil.of("2024-01-01", "2023", "2026-01-01 23:59:59", null, "Wed Aug 01 00:00:00 CST 2024", new Date()))
                .sortAsTimes(e -> e).nullLast()
                .sort()
        );


    }



}
