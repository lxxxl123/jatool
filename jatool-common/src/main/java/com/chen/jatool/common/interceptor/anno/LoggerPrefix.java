package com.chen.jatool.common.interceptor.anno;

import java.lang.annotation.*;

/**
 * 作用：
 *  在方法加上后，方法内（包括嵌套方法）所有通过@Slf4j打印的日志都会增加日志前缀
 *  若嵌套方法中有多个@LoggerPrefix，前缀则会叠加
 *  LoggerPrefixHolder.peek()可查看当前前缀
 * @author chenwh3
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LoggerPrefix {

    /**
     * 增加固定字符前缀
     */
    String value() default "";

    /**
     * 增加uuid前缀
     */
    boolean uuid() default true;

    /**
     * uuid长度，-1即32位长度
     */
    int uuidLen() default -1;
    /**
     * 是否打印方法的入参和出参
     */
    boolean[] logInAndOut() default {};
}
