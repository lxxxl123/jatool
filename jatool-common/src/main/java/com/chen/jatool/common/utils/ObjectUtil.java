package com.chen.jatool.common.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.Numbers;
import com.chen.jatool.common.utils.support.bean.BeanAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 对象工具类
 *
 * @author L.cm
 */
@Slf4j
public class ObjectUtil {

    /**
     * 判断元素不为空
     */
    public static boolean isNotEmpty(@Nullable Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isEmpty(@Nullable Object obj) {
        return ObjectUtils.isEmpty(obj);
    }

    public static boolean nullSafeEquals(@Nullable Object obj1, @Nullable Object obj2){
        return ObjectUtils.nullSafeEquals(obj1, obj2);
    }

    public static boolean isArray(@Nullable Object obj) {
        return ObjectUtils.isArray(obj);
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

    public static String removeSubAndPre(Object ori, String subAndPre) {
        if (ori == null) {
            return "";
        }
        String res = ori.toString().trim();
        if (StrUtil.isBlank(subAndPre)) {
            return res;
        }
        if (res.startsWith(subAndPre)) {
            res = res.substring(subAndPre.length());
        }
        if (res.endsWith(subAndPre)) {
            res = res.substring(0, res.length() - subAndPre.length());
        }
        return res;
    }

    public static Object get(Object obj, String field) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).get(field);
        } else {
            return new BeanAccessor(obj).get(field);
//            return ReflectUtil.getFieldValue(obj, field);
        }
    }

    public static String getStr(Object obj, String field) {
        return Convert.toStr(get(obj, field));
    }

    public static Integer getInt(Object obj, String field) {
        return Convert.toInt(get(obj, field));
    }

    public static void set(Object obj, String field, Object val) {
        if (obj == null) {
            return;
        }
        if (obj instanceof Map) {
            ((Map) obj).put(field, val);
        } else {
            new BeanAccessor(obj).set(field, val);
        }
    }

    public static <R> R merge(R obj1, R obj2) {
        if (obj1 == null) {
            return obj2;
        }
        if (obj2 == null) {
            return obj1;
        }
        if (obj1 instanceof Collection && obj2 instanceof Collection) {
            // 性能损失小点
            Collection<?> l1 = CollUtils.tryToColl(obj1);
            Collection<?> l2 = CollUtils.tryToColl(obj2);
            try {
                l1.addAll((Collection) l2);
            } catch (UnsupportedOperationException e) {
                log.warn("merge warning ", e);
                if (l1 instanceof List) {
                    l1 = CollUtils.toList(l1);
                    l1.addAll((Collection) l2);
                    return (R) l1;
                } else if (l1 instanceof Set) {
                    l1 = CollUtils.toSet(l1);
                    l1.addAll((Collection) l2);
                    return (R) new HashSet<>(l1);
                } else {
                    throw new ServiceException("no support");
                }
            }
            return (R) Convert.convert(obj1.getClass(), l1);
        }

        if (obj1 instanceof Map && obj2 instanceof Map) {
            HashMap<Object, Object> map = new HashMap<>();
            map.putAll((Map) obj1);
            map.putAll((Map) obj2);
            return (R) map;
        }
        if (obj1 instanceof Number && obj2 instanceof Number) {
            return (R) Convert.convert(obj1.getClass(), Numbers.of(obj1).add(obj2).getDecimal());
        }
        throw new ServiceException(StrUtil.format("not support merge type between {} and {}", obj1.getClass(), obj2.getClass()));
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T firstNotNull(T... args) {
        if (args == null) {
            return null;
        }
        for (T t : args) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
//        long start = System.currentTimeMillis();
//        // 1亿 3秒
//        for (int i = 0; i < 100000000; i++) {
////            Object invoke = BeanUtils.getPropertyDescriptor(f.getClass(), "checkpoints").getReadMethod().invoke(f);
//            ObjectUtil.get(f, "checkpoints");
//        }
//        System.out.println(System.currentTimeMillis() - start);
    }

}

