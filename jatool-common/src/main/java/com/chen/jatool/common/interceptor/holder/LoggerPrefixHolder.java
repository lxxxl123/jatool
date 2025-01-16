package com.chen.jatool.common.interceptor.holder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.MDC;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 管理日志前缀
 * @author chenwh3
 */
public class LoggerPrefixHolder {

    private static final ThreadLocal<Deque<String>> LOG_PREFIX_MSG = ThreadLocal.withInitial(ConcurrentLinkedDeque::new);
    public static final String MDC_PREFIX = "prefix";

    private LoggerPrefixHolder() {
    }


    public static void push(String msg) {
        LOG_PREFIX_MSG.get().addLast(msg);
        tryPushMdcPrefix();
    }

    public static void poll() {
        LOG_PREFIX_MSG.get().pollLast();
        tryPushMdcPrefix();
    }

    public static Deque<String> get() {
        return LOG_PREFIX_MSG.get();
    }

    public static Deque<String> getCopy() {
        return new ConcurrentLinkedDeque<>(LOG_PREFIX_MSG.get());
    }

    public static void set(Deque<String> list) {
        LOG_PREFIX_MSG.set(list);
        tryPushMdcPrefix();
    }


    private static void tryPushMdcPrefix() {
        Deque<String> list = LOG_PREFIX_MSG.get();
        if (list.size() > 0) {
            MDC.put(MDC_PREFIX, StrUtil.wrap(CollUtil.join(list, "-"), "[", "] "));
        } else {
            MDC.remove(MDC_PREFIX);
        }
    }

    /**
     * 不需要专门调用
     */
    public static void clear() {
        LOG_PREFIX_MSG.get().clear();
        MDC.clear();
    }

}

