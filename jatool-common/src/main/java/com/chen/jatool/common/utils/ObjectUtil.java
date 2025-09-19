package com.chen.jatool.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.CombineTool;
import com.chen.jatool.common.utils.support.Numbers;
import com.chen.jatool.common.utils.support.bean.BeanAccessor;
import com.chen.jatool.common.utils.support.bean.ObjectAccessor;
import com.chen.jatool.common.utils.support.lambda.Func1;
import com.chen.jatool.common.utils.support.lambda.Func2;
import com.chen.jatool.common.utils.support.lambda.LambdaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Predicate;

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


    public static Object get(Object obj, List<String> field) {
        if (obj == null) {
            return null;
        }
        for (String f : field) {
            Object res = obj = get(obj, f);
            if (obj != null) {
                return res;
            }
        }
        return null;
    }

    public static String getStr(Object obj, String field) {
        return getStr(obj, field, null);
    }

    public static String getStr(Object obj, String field, String orElse) {
        return Convert.toStr(get(obj, field), orElse);
    }
    public static String getStr(Object obj, List<String> field) {
        return Convert.toStr(get(obj, field));
    }

    public static Integer getInt(Object obj, String field) {
        return getInt(obj, field, null);
    }

    public static Integer getInt(Object obj, String field, Integer orElse) {
        return Convert.toInt(get(obj, field), orElse);
    }
    public static Integer getInt(Object obj, List<String> field) {
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



    public static <T> T compute(Object obj, String fieldName, Predicate<Object> doIf, Func1<Object, T> setVal) {
        Object val = get(obj, fieldName);
        if (doIf == null || doIf.test(val)) {
            T res = setVal.apply(val);
            set(obj, fieldName, res);
            return res;
        }
        return null;
    }

    public static <T, F> T compute(Object obj, Func1<F, Object> field, Func1<Object, T> setVal) {
        String fieldName = LambdaUtils.getFieldName(field);
        return compute(obj, fieldName, null, setVal);
    }



    public static <T> T compute(Object obj, String fieldName, Func1<Object, T> setVal) {
        return compute(obj, fieldName, null, setVal);
    }

    public static <T> T computeIfAbsent(Object obj, String fieldName, Func1<Object, T> setVal) {
        return compute(obj, fieldName, Objects::isNull, setVal);
    }


    public static <T, F> T computeIfPresent(Object obj, Func1<F, Object> field, Func1<Object, T> setVal) {
        String fieldName = LambdaUtils.getFieldName(field);
        return compute(obj, fieldName, Objects::nonNull, setVal);
    }


    public static <T> T computeIfPresent(Object obj, String fieldName, Func1<Object, T> setVal) {
        return compute(obj, fieldName, Objects::nonNull, setVal);
    }

    public static <T, F> T computeIfNotBlank(Object obj, Func1<F, Object> field, Func1<Object, T> setVal) {
        String fieldName = LambdaUtils.getFieldName(field);
        return compute(obj, fieldName, ObjectUtil::isNotBlank, setVal);
    }

    public static <T> T computeIfNotBlank(Object obj, String fieldName, Func1<Object, T> setVal) {
        return compute(obj, fieldName, ObjectUtil::isNotBlank, setVal);
    }

    public static void setIfAbsent(Object obj, String field, Object val) {
        if (get(obj, field) == null) {
            set(obj, field, val);
        }
    }

    public static void setIfBlank(Object obj, String field, Object val) {
        if (isBlank(get(obj, field))) {
            set(obj, field, val);
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
            Collection<Object> l1 = (Collection) CollUtils.tryToModifiableColl(obj1);
            Collection<?> l2 = (Collection<?>) obj2;
            try {
                l1.addAll(l2);
            } catch (UnsupportedOperationException e) {
                log.warn("merge warning ", e);
                if (l1 instanceof List) {
                    l1 = new ArrayList<>(l1);
                    l1.addAll(l2);
                    return (R) l1;
                } else if (l1 instanceof Set) {
                    l1 = new HashSet<>(l1);
                    l1.addAll(l2);
                    return (R) l1;
                } else {
                    throw new ServiceException("no support");
                }
            }
            return (R) l1;
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

    @SafeVarargs
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
    public static <O> void fillNullField(O target, O source) {
        fillFieldIf(target, source, (key, val) -> val == null);
    }

    public static <O> void fillBlankField(O target, O source) {
        fillFieldIf(target, source, (key, val) -> isBlank(val));
    }
    public static <O> void fillFieldWithKeys(O target, O source, Collection<String> keys) {
        fillFieldIf(target, source, (key, val) -> keys.contains(key));
    }

    public static <O> void fillFieldIf(O target, O source, Func2<String, Object, Boolean> setIf) {
        if (target == null || source == null) {
            return;
        }
        ObjectAccessor sourceAcc = ObjectAccessor.of(source);
        ObjectAccessor targetAcc = ObjectAccessor.of(target);
        targetAcc.keys().forEach(key -> {
            if (setIf == null || setIf.apply(key, targetAcc.get(key))) {
                targetAcc.set(key, sourceAcc.get(key));
            }
        });
    }

    public static void main(String[] args) throws Exception {
    }

}

