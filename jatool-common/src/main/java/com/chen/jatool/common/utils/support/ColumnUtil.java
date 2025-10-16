package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.chen.jatool.common.entity.sys.SysExtraColumn;
import com.chen.jatool.common.service.sys.SysExtraColumnServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author chenwh3
 */
@Component
public class ColumnUtil {

    public static final String COL_MESSAGE = "message";
    public static final String COL_REMARK = "remark";

    private static SysExtraColumnServiceImpl service;

    @Autowired
    public void init(SysExtraColumnServiceImpl service) {
        ColumnUtil.service = service;
    }



    public static <T> void batchLog(List<T> list, Func1<T, ?> func) {
        String fieldName = LambdaUtil.getFieldName(func);
        service.batchUpdate(list, null, null, fieldName, null);
    }

    public static <T> void batchLog(List<T> list, Func1<T, ?> getValField, Function<T,?> getVal) {
        String valueField = LambdaUtil.getFieldName(getValField);
        service.batchUpdate(list, null, null, valueField, getVal);
    }

    public static <T> void batchLog(List<T> list, Func1<T, ?> getKeyField, Func1<T, ?> getValField, Function<T,?> getVal) {
        batchLog(list, null, getKeyField, getValField, getVal);
    }

    public static <T> void batchLog(List<T> list, Class<?> clazz, Func1<T, ?> getKeyField, Func1<T, ?> getValField) {
        batchLog(list, clazz, getKeyField, getValField, null);
    }

    public static <T> void batchLog(List<T> list, Class<?> clazz, Func1<T, ?> getKeyField, Func1<T, ?> getValField, Function<T,?> getVal) {
        String keyProperty = getKeyField != null ? LambdaUtil.getFieldName(getKeyField) : null;
        String fieldName = getValField != null ? LambdaUtil.getFieldName(getValField) : null;
        service.batchUpdate(list, clazz, keyProperty, fieldName, getVal);
    }


    public static <T> void batchLog(List<T> list, Class<?> clazz, String keyProperty, String fieldName, Function<T,?> getVal) {
        service.batchUpdate(list, clazz, keyProperty, fieldName, getVal);
    }

    public static <T> void log(T obj, Func1<T, ?> getValueField) {
        String valueField = LambdaUtil.getFieldName(getValueField);
        service.batchUpdate(ListUtil.of(obj), null, null, valueField, null);
    }

    public static <T> void logMessage(T obj, String megName , String message) {
        service.batchUpdate(ListUtil.of(obj), null, null, megName, (e) -> message);
    }

    public static <T> void logMessage(T obj,  String message) {
        service.batchUpdate(ListUtil.of(obj), null, null, COL_MESSAGE, (e) -> message);

    }

    public static <T> void batchFill(List<T> list, Class<?> clazz,Func1<T, ?> getKeyField, Func1<T, ?> getValField, BiConsumer<T, SysExtraColumn> consumer) {
        String keyField = getKeyField != null ? LambdaUtil.getFieldName(getKeyField) : null;
        String valueField = getValField != null ? LambdaUtil.getFieldName(getValField) : null;
        service.batchFill(list, clazz, keyField, valueField, consumer);
    }

    public static <T> void batchFill(List<T> list, Class<?> clazz, String keyField, String valueField) {
        batchFill(list, clazz, keyField, valueField, null);
    }

    public static <T> void batchFill(List<T> list, Class<?> clazz, String keyField, String valueField, BiConsumer<T, SysExtraColumn> consumer) {
        service.batchFill(list, clazz, keyField, valueField, consumer);
    }


}
