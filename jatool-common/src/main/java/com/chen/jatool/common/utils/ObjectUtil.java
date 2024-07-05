package com.chen.jatool.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.Numbers;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 对象工具类
 *
 * @author L.cm
 */
public class ObjectUtil extends org.springframework.util.ObjectUtils {

    /**
     * 判断元素不为空
     *
     * @param obj object
     * @return boolean
     */
    public static boolean isNotEmpty(@Nullable Object obj) {
        return !ObjectUtil.isEmpty(obj);
    }

    public static boolean isNotBlank(@Nullable Object obj) {
        return !isBlank(obj);
    }

    public static boolean isBlank(Object obj) {
        return obj == null || obj instanceof String && StrUtil.isBlank((String) obj);
    }

    public static boolean equal(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equalsWithAny(Object obj, Object... any) {
        for (Object o : any) {
            if (ObjectUtil.equal(obj, o)) {
                return true;
            }
        }
        return false;
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

    public static Object merge(Object obj1, Object obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        if (obj1 instanceof Collection || obj2 instanceof Collection
                || obj1 instanceof Stream || obj2 instanceof Stream
                || obj1 instanceof Iterable || obj2 instanceof Iterable) {
            return Convert.convert(obj1.getClass(), CollUtils.toList(obj1).addAll((Collection) CollUtils.toList(obj2)));
        }

        if (obj1 instanceof Map && obj2 instanceof Map) {
            HashMap<Object, Object> map = new HashMap<>();
            map.putAll((Map) obj1);
            map.putAll((Map) obj2);
            return map;
        }
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return Convert.convert(obj1.getClass(), Numbers.of(obj1).add(obj2).getDecimal());
        }
        throw new ServiceException("not support merge type between {} and {}", obj1.getClass(), obj2.getClass());
    }

}

