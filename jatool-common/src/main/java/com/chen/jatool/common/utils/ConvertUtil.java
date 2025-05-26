package com.chen.jatool.common.utils;

import cn.hutool.core.convert.Converter;
import cn.hutool.core.convert.ConverterRegistry;
import com.chen.jatool.common.utils.support.DateTimes;
import com.chen.jatool.common.utils.support.Numbers;

/**
 * 基本类型转换
 */
public class ConvertUtil {
    private static final ConverterRegistry converterRegistry = ConverterRegistry.getInstance();

    /**
     * 只支持基本类型，见{@link ConverterRegistry#defaultConverter()}
     */
    public static <T> Converter<T> getConverter(Class<T> clazz){
        return converterRegistry.getDefaultConverter(clazz);
    }

    public static <T> T convert(Object obj, Class<T> clazz, T defaultValue) {
        if (obj == null) {
            return null;
        }
        try {
            if (DateTimes.class.equals(clazz)) {
                return (T) DateTimes.of(obj);
            }
            if (Numbers.class.equals(clazz)) {
                return (T) Numbers.of(obj);
            }
        } catch (Exception e) {
            return defaultValue;
        }
        Converter<T> converter = getConverter(clazz);
        if (converter != null) {
            return converter.convert(obj, defaultValue);
        }
        return null;
    }


}
