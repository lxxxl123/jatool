package com.chen.jatool.common.utils;

import cn.hutool.core.lang.UUID;
import com.chen.jatool.common.interceptor.holder.LoggerPrefixHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenwh3
 */
@Slf4j
public class ThreadUtils {

    public static final Executor COMMON_EXECUTOR = build("common-executor", 4, 16, 100, 60);

    /**
     * 可以把基本的线程信息复制到子线程中
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, COMMON_EXECUTOR);
    }


    private static Executor build(String name, int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        if (name != null) {
            executor.setThreadGroupName(name);
            executor.setThreadNamePrefix(name);
        }
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setTaskDecorator((runnable) -> {
//            LoginVo loginVo = UserUtil.tryGetLoginVo();
//            String token = JwtTokenUtil.tryGetToken();
            // 日志前缀
            Deque<String> mdc = LoggerPrefixHolder.getCopy();
//            String peek = DynamicDataSourceContextHolder.peek();
            return () -> {
                // 子线程没有用户信息 , 所以需要从主线程中获取 , 并在子线程中设置用户信息
                try {
                    LoggerPrefixHolder.set(mdc);
                    LoggerPrefixHolder.push(UUID.fastUUID().toString(true).substring(0, 9));
//                    DynamicDataSourceContextHolder.push(peek);
//                    LoginVoHolder.set(loginVo);
//                    TokenHolder.set(token);
                    runnable.run();
                } catch (Exception e) {
                    log.error("", e);
                } finally {
//                    if (ExceptionHolder.hasEx()) {
//                        log.error("", ExceptionHolder.get());
//                    }
//                    TokenHolder.clear();
//                    ExceptionHolder.clear();
//                    DynamicDataSourceContextHolder.clear();
//                    LoginVoHolder.clear();
//                    StopWatchHolder.flushAndLog();
                    LoggerPrefixHolder.clear();
                }
            };
        });
        executor.initialize();
        return executor;
    }

}
