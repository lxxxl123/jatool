package com.chen.jatool.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * 对象工具类
 *
 * @author L.cm
 */
public class ObjectUtil extends cn.hutool.core.util.ObjectUtil {

    /**
     * 判断元素不为空
     * @param obj object
     * @return boolean
     */
    public static boolean isNotEmpty(@Nullable Object obj) {
        return !ObjectUtil.isEmpty(obj);
    }

    public static boolean isNotBlank(@Nullable Object obj) {
        return !isBlank(obj);
    }

    public static boolean isBlank(Object obj){
        return obj == null || obj instanceof String && StrUtil.isBlank((String) obj);
    }

    public static Object tryTrim(Object obj) {
        if (obj instanceof String) {
            return StrUtil.trim((String) obj);
        }
        return obj;
    }

    public static Object get(Object obj, String field) {
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(field);
        } else {
            return ReflectUtil.getFieldValue(obj, field);
        }
    }

    public static String getStr(Object obj, String field) {
        return Convert.toStr(get(obj, field));
    }

    public static Integer getInt(Object obj, String field) {
        return Convert.toInt(get(obj, field));
    }

    public static void set(Object obj, String field, Object val) {
        if (obj instanceof Map) {
            ((Map) obj).put(field, val);
        } else {
            ReflectUtil.setFieldValue(obj, field, val);
        }
    }

}

