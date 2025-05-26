package com.chen.jatool.common.interceptor;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.interceptor.anno.LoggerPrefix;
import com.chen.jatool.common.interceptor.holder.LoggerPrefixHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 高优先级
 * @author chenwh3
 */
@Component
@Aspect
@Slf4j
@Order(-200)
public class LoggerPrefixAspect {

    @Around("@annotation(com.haday.qms.interceptor.anno.LoggerPrefix)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable{
        LoggerPrefix loggerPrefix = ((MethodSignature)pjp.getSignature()).getMethod().getAnnotation(LoggerPrefix.class);
        int i = 0;
        if (StrUtil.isNotBlank(loggerPrefix.value())) {
            LoggerPrefixHolder.push(loggerPrefix.value());
            i++;
        }
        if (loggerPrefix.uuid()) {
            String uuidstr = IdUtil.fastSimpleUUID();
            if (loggerPrefix.uuidLen() > 0) {
                uuidstr = uuidstr.substring(0, loggerPrefix.uuidLen());
            } else if (i > 0) {
                uuidstr = uuidstr.substring(0, 16);
            }
            LoggerPrefixHolder.push(uuidstr);
            i++;
        }
        boolean[] ioArr = loggerPrefix.logInAndOut();
        boolean logIn = ioArr.length > 0 && ioArr[0];
        boolean logOut = ioArr.length > 1 ? ioArr[1] : logIn;
        if (logIn) {
            log.info("params = {}", pjp.getArgs());
        }
        Object res = null;
        try {
            res = pjp.proceed();
            return res;
        } finally {
            if (logOut) {
                log.info("return = {}", res);
            }
            while ((i = i - 1) >= 0) {
                LoggerPrefixHolder.poll();
            }
        }
    }


}
