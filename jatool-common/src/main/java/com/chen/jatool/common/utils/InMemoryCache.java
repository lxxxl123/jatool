package com.chen.jatool.common.utils;


import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author chenwh3
 */
@ToString
@Slf4j
public class InMemoryCache<K,V> {

    private final Map<K, CacheItem<V>> cache = new ConcurrentHashMap<>();

    private final long defaultExpirationTimeMillis;


    private final int maxEntries;

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);


    public InMemoryCache(long defaultExpirationTimeMillis, int maxEntries) {
        this.defaultExpirationTimeMillis = defaultExpirationTimeMillis;
        this.maxEntries = maxEntries;
        SCHEDULER.scheduleAtFixedRate(this::sweep, 30, 30, TimeUnit.MINUTES);
    }

    // Default: 1 minute expiration time, extend by 30 seconds on access
    public InMemoryCache() {
        this(60 * 1000,  100);
    }

    public void put(K key, V value, long expirationTimeMillis) {
        if (cache.size() >= maxEntries) {
            sweep();
        }
        cache.put(key, new CacheItem<>(value,  expirationTimeMillis));
    }

    public void put(K key, V value) {
        put(key, value, defaultExpirationTimeMillis);
    }

    public Object get(K key) {
        CacheItem<V> item = cache.get(key);
        if (item != null && item.isValid()) {
            item.extend();
            return item.getValue();
        } else {
            remove(key);
            return null;
        }
    }

    public boolean containsKey(K key) {
        CacheItem<V> item = cache.get(key);
        return item != null && item.isValid();
    }

    public synchronized void sweep() {
        log.info("开始清理内存缓存 , 缓存数量 = {}", cache.size());
        List<K> keysToRemove = new ArrayList<>();
        for (Map.Entry<K, CacheItem<V>> entry : cache.entrySet()) {
            K key = entry.getKey();
            CacheItem<V> item = entry.getValue();
            if (!item.isValid()) {
                keysToRemove.add(key);
            }
        }
        for (K keyToRemove : keysToRemove) {
            cache.remove(keyToRemove);
        }
        if (cache.size() >= maxEntries) {
            cache.clear();
        }
        log.info("清理内存缓存完成 , 缓存数量 = {}", cache.size());
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public Map<K, CacheItem<V>> peek(){
        return Collections.unmodifiableMap(cache);
    }

    public void clear(){
        cache.clear();
    }

    @Data
    @ToString
    private static class CacheItem<V> {

        private final SoftReference<V> reference;

        private long expirationTimeMillis;

        private long deadLine;

        public CacheItem(V value , long expirationTimeMillis) {
            this.reference = new SoftReference<>(value);
            this.expirationTimeMillis = expirationTimeMillis;
            this.deadLine = System.currentTimeMillis() + expirationTimeMillis;
        }

        public V getValue() {
            return reference.get();
        }

        public boolean isValid() {
            return System.currentTimeMillis() < deadLine && reference.get() != null;
        }

        public synchronized void extend() {
            deadLine = System.currentTimeMillis() + expirationTimeMillis;;
        }


        public synchronized void extendExpirationTime(long extensionTimeMillis) {
            deadLine += extensionTimeMillis;
        }
    }


}