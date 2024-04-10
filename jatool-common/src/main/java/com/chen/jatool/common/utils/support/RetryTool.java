package com.chen.jatool.common.utils.support;

import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * @author chenwh3
 */
@Slf4j
public class RetryTool {

    public RetryTool(){
    }

    private int retry = 3;

    private String taskName = "-";

    public static RetryTool of() {
        return new RetryTool();
    }



    public RetryTool taskName(String taskName, Object... objs) {
        this.taskName = StrUtil.format(taskName, objs);
        return this;
    }

    public RetryTool retry(int retry) {
        this.retry = retry;
        if (retry < 0) {
            throw new IllegalArgumentException("" + retry);
        }
        return this;
    }

    public <T> T get(Supplier<T> func) {
        return get(func, this.retry);
    }

    private  <T> T get(Supplier<T> func, int retry) {
        while (retry-- > 0) {
            try {
                T res = func.get();
                return res;
            } catch (Exception e) {
                if (retry == 0) {
                    log.error(StrUtil.format("task = {}, retry {} times and all failed", taskName, this.retry), e);
                    throw e;
                } else {
                    log.error(StrUtil.format("task = {}, remain times = {}", taskName, retry), e);
                    return get(func, retry);
                }
            }
        }
        throw new ServiceException("retry failed");
    }

    public void run(Runnable runnable){
        get(() -> {
            runnable.run();
            return null;
        });
    }

}
