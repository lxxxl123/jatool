package com.chen.jatool.common.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.Converter;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.utils.support.DateTimes;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author chenwh3
 */
@Slf4j
public class BeanUtils {


    public static TypeConverter DEFUALT_CONVERTER = Convert::convert;
    public static TypeConverter DATE_CONVERTER = (t, v)->{
        if (Date.class.equals(t)) {
            if (ObjectUtil.isBlank(v)) {
                return null;
            }
            return DateTimes.parse(v);
        }
        return DEFUALT_CONVERTER.convert(t,v);
    };

    public static Class<?> getListType(Field field) {
        try {
            Class<?> type = field.getType();
            if (List.class.isAssignableFrom(type)) {
                return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static TypeConverter UTC_DATE_CONVERTER = (t, v)->{
        if (Date.class.equals(t)) {
            if (ObjectUtil.isBlank(v)) {
                return null;
            }
            Date date = DateTimes.parse(v);
            if (date.getTime() < 0) {
                return Date.from(Instant.EPOCH);
            }
        }
        return DEFUALT_CONVERTER.convert(t,v);
    };

    public static final CopyOptions COMMON_OPTIONS = CopyOptions.create()
            .setFieldNameEditor(StrUtil::toCamelCase)
            .setConverter(DATE_CONVERTER);

    public static final CopyOptions IGNORECASE_OPTIONS = CopyOptions.create()
            .ignoreCase()
            .setFieldNameEditor(StrUtil::toCamelCase)
            .setConverter(DATE_CONVERTER);

    /**
     * 忽略错误日期，sqlserver datetime最小日期为 1753-01-01
     * 因此这里的处理方式为小于 1970-01-01的日期，全部置为1970-01-01
     */
    public static final CopyOptions MDM_OPTIONS = CopyOptions.create()
            .setFieldNameEditor(StrUtil::toCamelCase)
            .setConverter(UTC_DATE_CONVERTER);



    public static  <T> List<T> arrayToBean(List array, Class<T> clazz) {
        return arrayToBean(array, clazz, COMMON_OPTIONS);
    }

    public static  <T> List<T> arrayToBean(List array, Class<T> clazz, CopyOptions copyOptions) {
        if (CollUtil.isEmpty(array)) {
            return Collections.emptyList();
        }
        List<T> res = new ArrayList<>(array.size());
        array.forEach(e->{
            res.add(toBean(e, clazz,copyOptions));
        });
        return res;
    }


    public static <T> T toBean(Object o, Class<T> clazz) {
        return toBean(o, clazz, COMMON_OPTIONS);
    }

    public static <T> T toBean(Object o, Class<T> clazz, CopyOptions copyOptions) {
        if (o == null) {
            return null;
        }
        Converter<T> converter = ConvertUtil.getConverter(clazz);
        if (converter != null) {
            return converter.convert(o, null);
        }
        return BeanUtil.toBean(o, clazz, copyOptions);
    }

    public static <T, S> T copy(S source, T target, CopyOptions copyOptions) {
        BeanUtil.copyProperties(source, target, copyOptions);
        return target;
    }

    public static <T, S> T copy(S source, T target) {
        return copy(source, target, COMMON_OPTIONS);
    }

    public static <T> T copyIgnoreCase(Object source, T target) {
        return copy(source, target, IGNORECASE_OPTIONS);
    }

}
