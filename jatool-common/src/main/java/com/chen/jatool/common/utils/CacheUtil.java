package com.chen.jatool.common.utils;


import com.chen.jatool.common.utils.support.InMemoryCache;
import com.haday.qms.core.tool.support.InMemoryCache;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Function;

/**
 * @author chenwh3
 */
@Slf4j
public class CacheUtil {
    /**
     * 内存缓存默认1分钟 , 最大缓存500个
     */
    private static final InMemoryCache<String, Object> IN_MEMORY_CACHE = new InMemoryCache<>(60 * 1000, 10000);

    /**
     * 半小时
     */
    private static long MAX_CACHE_TIMEOUT = 1000 * 60 * 30;


    public static Object get(String key) {
        return IN_MEMORY_CACHE.get(key);
    }

    /**
     * @param timeout ms
     */
    public static void set(String key, Object value, long timeout) {
        timeout = Math.min(timeout, MAX_CACHE_TIMEOUT);
        IN_MEMORY_CACHE.put(key, value, timeout);
    }

    @SuppressWarnings("unchecked")
    public static <T> T computeIfAbsent(String key, Function<String, T> func, long timeout) {
        timeout = Math.min(timeout, MAX_CACHE_TIMEOUT);
        return (T) IN_MEMORY_CACHE.computeIfAbsent(key, (Function<String, Object>) func, timeout);
    }

    public static void remove(String key) {
        IN_MEMORY_CACHE.remove(key);
    }

    public static void removePrefix(String key) {
        IN_MEMORY_CACHE.removePrefix(key);
    }

    public static Map peek() {
        return IN_MEMORY_CACHE.peek();
    }

    public static void clear(){
        IN_MEMORY_CACHE.clear();

    }

}
