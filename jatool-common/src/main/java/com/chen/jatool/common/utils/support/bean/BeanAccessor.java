package com.chen.jatool.common.utils.support.bean;

import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author chenwh3
 */
public class BeanAccessor extends ObjectAccessor {

    private Object target;

    private Map<String, Field> fieldMap;

    private List<String> keyList;

    private static Map<Class, List<String>> classKeyList = new HashMap<>();
    private static Map<Class, Map<String, Field>> classFieldCache = new HashMap<>();


    public BeanAccessor(Object target) {
        this.target = target;
        final Class targetClass = target.getClass();

        fieldMap = classFieldCache.computeIfAbsent(targetClass, (k) -> {
            Map<String, Field> fieldMap = new HashMap<>();
            Class<?> cur = targetClass;
            do {
                Field[] fields = cur.getDeclaredFields();
                for (int i = 0; i < fields.length; ++i) {
                    Field field = fields[i];
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    // 同属性名的情况下，避免父类覆盖子类的属性
                    fieldMap.putIfAbsent(field.getName(), fields[i]);
                }
                cur = cur.getSuperclass();
            } while (cur != null && cur != Object.class);
            return fieldMap;
        });
    }

    public void set(String name, Object value) {
        Field field = fieldMap.get(name);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
            try {
                Class<?> type = field.getType();
                if (List.class.isAssignableFrom(type)) {
                    field.set(target, BeanUtils.arrayToBean((List) value, BeanUtils.getListType(field)));
                } else {
                    field.set(target, BeanUtils.toBean(value, field.getType()));
                }
            } catch (IllegalAccessException e) {
                throw new ServiceException(e);
            }
        }
    }

    public Object get(String name) {
        Field field = fieldMap.get(name);
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                throw new ServiceException(e);
            }
        }
        return null;
    }

    @Override
    public List<String> keys() {
        keyList = classKeyList.computeIfAbsent(target.getClass(), (k) -> {
            List<String> list = new ArrayList<>();
            Set<Map.Entry<String, Field>> entries = fieldMap.entrySet();
            for (Map.Entry<String, Field> entry : entries) {
                list.add(entry.getValue().getName());
            }
            return list;
        });
        return keyList;
    }
}
