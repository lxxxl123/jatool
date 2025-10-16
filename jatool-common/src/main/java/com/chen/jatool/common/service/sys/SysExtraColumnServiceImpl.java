package com.chen.jatool.common.service.sys;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.jatool.common.entity.sys.SysExtraColumn;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import com.chen.jatool.common.utils.support.CombineTool;
import com.chen.jatool.common.utils.support.sql.PlainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用于存储仅展示，不参与运算的字段。
 *
 * @author chenwh3
 */
@Service
@Slf4j
public class SysExtraColumnServiceImpl  {


    @Resource
    private SysExtraColumnServiceImpl service;

    public Integer batchMerge(List<SysExtraColumn> list) {
        throw new SecurityException("not support");
    }

    public List<SysExtraColumn> getListBy( PlainWrapper of) {
        throw new SecurityException("not support");
    }


    @Transactional(rollbackFor = Exception.class)
    public <T> void batchUpdate(List<T> list, Class<?> clazz, String keyProperty, String colName, Function<T, ?> getVal) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        if (clazz == null) {
            clazz = list.get(0).getClass();
        }
        TableInfo info = TableInfoHelper.getTableInfo(clazz);
        String tableName = info.getTableName();
        keyProperty = keyProperty == null ? info.getKeyProperty() : keyProperty;
        if (StrUtil.isBlank(tableName)) {
            throw new ServiceException("tableName is null");
        }
        Map<Integer, String> ids = new HashMap<>(list.size());
        for (T t : list) {
            Integer id = ObjectUtil.getInt(t, keyProperty);
            String val;
            if (getVal != null) {
                val = Convert.toStr(getVal.apply(t));
            } else {
                val = ObjectUtil.getStr(t, colName);
            }
            if (id == null) {
                throw new ServiceException("id is null");
            }
            ids.put(id, val);
        }
        List<SysExtraColumn> mergeList = ids.entrySet().stream().map(e -> {
            SysExtraColumn column = new SysExtraColumn();
            column.setBusId(e.getKey());
            column.setTableName(tableName);
            column.setCol(colName);
            column.setValue(e.getValue());
            return column;
        }).filter(e -> e.getValue() != null).collect(Collectors.toList());
        batchMerge(mergeList);
    }


    /**
     * 重载方法，增加map参数，用于自定义需要填充的字段
     */
    public <T> void batchFill(List<T> list, Class<?> clazz, String keyField, String valueField, BiConsumer<T, SysExtraColumn> consumer) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        if (clazz == null) {
            clazz = list.get(0).getClass();
        }
        TableInfo info = TableInfoHelper.getTableInfo(clazz);
        String tableName = info.getTableName().toLowerCase();


        Set<Integer> ids = new HashSet<>();
        for (T t : list) {
            Integer id = ObjectUtil.getInt(t, keyField);
            if (id == null) {
                continue;
            }
            ids.add(id);
        }
        if (ids.isEmpty()) {
            return;
        }

        List<SysExtraColumn> res = getListBy(PlainWrapper.of()
                .in(SysExtraColumn::getBusId, ids)
                .eq(SysExtraColumn::getTableName, tableName)
                .eq(SysExtraColumn::getCol, valueField)
        );

        CombineTool.of(list, res)
                .eq(e -> ObjectUtil.getInt(e, keyField), SysExtraColumn::getBusId)
                .forEach((l, r) -> {
                    if (consumer != null) {
                        consumer.accept(l, r);
                    } else {
                        ObjectUtil.set(l, valueField, r.getValue());
                    }
                });
    }




}

