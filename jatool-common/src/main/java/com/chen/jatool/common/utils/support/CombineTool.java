package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.chen.jatool.common.utils.BeanUtils;
import com.chen.jatool.common.utils.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 用于合并数据
 *
 * @author chenwh3
 */
public class CombineTool<T, N> {

    List<T> driveTable;
    List<N> nonDriveTables;

    List<Function> leftFunc = new ArrayList<>();
    List<Function> rightFunc = new ArrayList<>();


    private boolean onlyTopOne = true;



    public CombineTool(List<T> driveTable, List<N> nonDriveTables) {
        this.driveTable = driveTable;
        this.nonDriveTables = nonDriveTables;
    }

    public static <T, N> CombineTool<T, N> of(List<T> driveTable, List<N> nonDriveTables) {
        return new CombineTool<>(driveTable, nonDriveTables);
    }
    /**
     * 默认left join
     */
    private boolean innerJoin = false;

    /**
     * 根据{@link #leftFunc}的数量 和 {@link #nonDriveTables} 生成n层索引 ;
     */
    private MultiKeyMap<Object, List<N>> index;

    private boolean trimKey = true;


    public CombineTool<T, N> onlyTopOne(boolean onlyTopOne) {
        this.onlyTopOne = onlyTopOne;
        return this;
    }


    public CombineTool<T, N> eq(Function<T, ?> f1, Function<N, ?> f2) {
        leftFunc.add(f1);
        rightFunc.add(f2);
        return this;
    }

    public CombineTool<T, N> innerJoin(boolean val) {
        this.innerJoin = val;
        return this;
    }



    private MultiKey getKey(Object map, int index) {
        try {
            Object[] keys = new Object[leftFunc.size()];
            for (int i = 0; i < leftFunc.size(); i++) {
                List<Function> func = index == 0 ? leftFunc : rightFunc;
                Object key = func.get(i).apply(map);
                if (trimKey && key instanceof String) {
                    key = StringUtil.toNotNullStr(key);
                }
                keys[i] = key;
            }
            return new MultiKey(keys);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MultiKey getRightKey(Object map) {
        return getKey(map, 1);
    }

    private MultiKey getLeftKey(Object map) {
        return getKey(map, 0);
    }

    /**
     * 使用索引快速找到对应的非驱动表相关的行
     */
    private List<N> getByIndex(Object row) {
        MultiKey key = getLeftKey(row);
        return index.get(key);
    }

    protected void buildIndex() {
        MultiKeyMap<Object, List<N>> indexMap = new MultiKeyMap<>();
        index = indexMap;
        if (CollUtil.isEmpty(nonDriveTables)) {
            return;
        }
        for (N map : nonDriveTables) {
            indexMap.computeIfAbsent(getRightKey(map), e -> new ArrayList<>())
                    .add(map);
        }
    }



    public void forEachFull(BiConsumer<T, N> forEachFunc){
        combine((a,b)->{
            forEachFunc.accept(a, b);
            return null;
        });
    }

    public void forEach(BiConsumer<T, N> forEachFunc){
        combine((a,b)->{
            if (b == null) {
                return null;
            }
            forEachFunc.accept(a, b);
            return null;
        });
    }

    public List<T> combine(){
        return combine((l,r)->{
            if (r != null) {
                BeanUtils.copy(r, l);
            }
            return l;
        });
    }

    public <A> List<A> combine(Class<A> clazz) {
        return combine((l,r)->{
            A res = BeanUtils.toBean(l, clazz);
            if (r != null) {
                BeanUtils.copy(r, res);
            }
            return res;
        });
    }

    public <A> List<A> combine(BiFunction<T, N, A> combineFunc) {
        if (CollectionUtils.isEmpty(driveTable) || CollectionUtils.isEmpty(leftFunc)) {
            return Collections.emptyList();
        }
        //对非驱动表建立索引
        buildIndex();

        List<A> res = new ArrayList<>();

        //遍历驱动表
        for (T driveRow : driveTable) {
            //查找对应的非驱动表数据
            List<N> nonDriveRows = getByIndex(driveRow);
            if (CollectionUtils.isEmpty(nonDriveRows)) {
                if (!innerJoin) {
                    res.addAll(buildRow(driveRow, null, combineFunc));
                }
            } else {
                //根据找到的数据拼接
                res.addAll(buildRow(driveRow, nonDriveRows, combineFunc));
            }
        }

        return res;
    }


    /**
     * 合并数据
     * 会合并同键的值 , 如有需要可继承定制
     */
    protected <A> List<A> buildRow(T driveRow, List<N> nonDriveRows, BiFunction<T, N, A> combineFunc) {
        if (CollUtil.isEmpty(nonDriveRows)) {
            return ListUtil.of(combineFunc.apply(driveRow, null));
        }
        List<A> res = new ArrayList<>();
        for (N nonDriveRow : nonDriveRows) {
            res.add(combineFunc.apply(driveRow, nonDriveRow));
            if (onlyTopOne) {
                break;
            }
        }
        return res;
    }

    public static void main(String[] args) {
        List<Map<String, Object>> list = ListUtil.of(Maps.ofArr("id", 1,"val",null).getStrObjMap());
//        List<Map<String, Object>> list2 = ListUtil.of(Maps.ofArr("id", 1, "val", 2).getStrObjMap());
        List<Map<String, Object>> list2 = null;

        CombineTool.of(list, list2)
                .eq(e -> e.get("id"), e -> e.get("id"))
                .forEach((l, r) -> {
                    System.out.println(l);
                    System.out.println(r);
                });
    }

}
