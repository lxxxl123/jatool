package com.chen.jatool.common.interceptor.aspect;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.interceptor.anno.MemCacheable;
import com.chen.jatool.common.utils.CacheUtil;
import com.chen.jatool.common.utils.SpringUtils;
import com.chen.jatool.common.utils.support.DateTimes;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;

@Component
@Aspect
@Slf4j
public class AspectUtils {

    private static String arrayToString(Object[] a) {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            Object v = a[i];
            if (v == null) {
                b.append("null");
            } else if (DateTimes.isSupportVal(v)) {
                // 去掉末尾0节省空间
                b.append(StrUtil.trim(String.valueOf(DateTimes.of(v).getEpochMillis()), 1, e -> e == '0'));
            } else {
                b.append(v);
            }
            if (i == iMax)
                return b.append(']').toString();
            b.append(",");
        }
    }

    public static LinkedHashMap<String, Object> getParamMap(JoinPoint joinPoint) {
        LinkedHashMap<String, Object> param = new LinkedHashMap<>(4);
        Object[] paramValues = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            param.put(paramNames[i], paramValues[i]);
        }
        return param;
    }

    @Around("@annotation(com.chen.jatool.common.interceptor.anno.MemCacheable)")
    public Object memoryCacheAround(ProceedingJoinPoint pjp) {
        try {
            Method method = ((MethodSignature) pjp.getSignature()).getMethod();
            MemCacheable anno = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(MemCacheable.class);

            String value = anno.value();
            String key = anno.key();
            if (StrUtil.isBlank(value)) {
                value = method.getDeclaringClass().getSimpleName() + "#" + method.getName();
            }
            String suffix;
            if (StrUtil.isBlank(key)) {
                suffix = arrayToString(pjp.getArgs());
            } else {
                suffix = SpringUtils.resolveEl(key, AspectUtils.getParamMap(pjp));
            }
            String mapKey ;
            if (StrUtil.isNotBlank(suffix)) {
                mapKey = value + ":" + suffix;
            } else {
                mapKey = value;
            }
//            if (anno.ds()) {
//                mapKey += ":" + DynamicDataSourceContextHolder.peek();
//            }
            long timeout = anno.timeout();
//            if (!SystemUtil.isProd()) {
//                // 测试环境2秒缓存
//                timeout = 2;
//            }

            return CacheUtil.computeIfAbsent(mapKey, (k) -> {
                try {
                    return pjp.proceed();
                } catch (Throwable e) {
                    throw ExceptionUtil.wrapRuntime(e);
                }
            }, timeout * 1000);
        } catch (Throwable e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
    }
}
