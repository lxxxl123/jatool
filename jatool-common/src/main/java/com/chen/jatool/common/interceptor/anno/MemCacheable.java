package com.chen.jatool.common.interceptor.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NULL值不会缓存
 * value和key都为空，则默认 类名#方法名:所有方法入参 , 如 SysParamsServiceImpl#listWerks:[args]
 * 内存缓存, 会定时清理 , timeout不宜过长 , 强制最长半小时
 * 使用软引用 , 内存不足时会自动清理
 * 本地环境内存缓存强制5秒，避免影响测试
 * mapKey = value + ":" + key , 支持el表达式
 * @author chenwh3
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MemCacheable {

    /**
     * 空则默认为类名+方法名
     */
    String value() default "";

    /**
     * 使用el表达式 #{} 具体参考 {@link com.chen.jatool.common.utils.SpelUtils#parseStr(String, Object)}
     * 空则会使用方法的所有参数作为缓存
     * 无参数则空
     */
    String key() default "";

    /**
     * 缓存加上当前的数据库标识
     */
    boolean ds() default false;

    /**
     * 单位: s
     * 默认5分钟
     * 非正式环境强制2秒缓存，方便测试
     */
    long timeout() default  60 * 5;


}
