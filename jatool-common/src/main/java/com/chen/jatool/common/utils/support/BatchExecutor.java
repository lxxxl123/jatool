package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.chen.jatool.common.exception.BatchExecuteException;
import com.chen.jatool.common.utils.CollUtils;
import com.chen.jatool.common.utils.ObjectUtil;
import com.chen.jatool.common.utils.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 作用: 分批执行 ; 遇到错误自动拆分继续执行
 *
 * @author chenwh3
 */
@Slf4j
public class BatchExecutor<T> {
    private final Integer batchSize;

    /**
     * 并且会开启重试，否则不会
     * 常用于插入事务
     */
    private boolean useTrans = false;

    /**
     * 开启重试
     */
    private boolean useRetry = false;

    private int retry = 3;

    private int retryDiv = 5;

    private int maxErrorCount = 50;

    private final AtomicInteger errorCount = new AtomicInteger(0);

    private final List<T> list;

    private Executor executor;

    public BatchExecutor(List<T> list, Integer batchSize, boolean partTrans, int retry, int retryDiv, int maxErrorCount) {
        this.list = list;
        this.batchSize = batchSize;
        this.retry = retry;
        this.retryDiv = retryDiv;
        this.maxErrorCount = maxErrorCount;
        nestedTrans(partTrans);
    }

    public BatchExecutor(List<T> list, Integer batchSize) {
        this.list = list;
        this.batchSize = batchSize;
    }

    public static <T> BatchExecutor<T> of(List<T> list, int size) {
        return new BatchExecutor<>(list, size);
    }


    public BatchExecutor<T> doRetry() {
        this.useRetry = true;
        return this;
    }


    /**
     * 若外层有事务,出错会全部回滚
     * 若cacheError，回滚取决于外层事务最终是否出错，否则回滚部分
     * 其他情况同newTrans
     */
    public BatchExecutor<T> nestedTrans(boolean bool) {
        this.useTrans = bool;
        trans.propagate(TransactionDefinition.PROPAGATION_NESTED);
        return this;
    }


    /**
     * 若外层有事务,出错只会回滚错误部分
     * 但外层有事务建议使用-nestedTrans，因多个事务数据可能有冲突
     */
    public BatchExecutor<T> newTrans(boolean bool) {
        this.useTrans = bool;
        trans.propagate(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return this;
    }

    public BatchExecutor<T> setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    private Consumer<BatchExecuteException> doCatchErr;
    private BiConsumer<List<List<T>>, List<Exception>> doCatchList;

    public BatchExecutor<T> catchException(Consumer<BatchExecuteException> consumer) {
        doCatchErr = consumer;
        return this;
    }

    public BatchExecutor<T> catchList(BiConsumer<List<List<T>>, List<Exception>> consumer) {
        doCatchList = consumer;
        return this;
    }



    private final List<List<T>> errorList = new CopyOnWriteArrayList<>();
    private final List<Exception> errEx = new CopyOnWriteArrayList<>();


    public void run(Consumer<List<T>> consumer) {
        get(e -> {
            consumer.accept(e);
            return null;
        });
    }

    /**
     * 谨慎使用
     */
    public void runAsync(Consumer<List<T>> consumer) {
        getAsync(e -> {
            consumer.accept(e);
            return null;
        });
    }

    public <R> R get(Function<List<T>, R> func) {
        List<T> o = this.list;
        if (batchSize <= 0 || CollUtil.size(o) <= 0) {
            return func.apply(o);
        }
        R res = batchExecute(o, func, batchSize);
        tryThrow();
        return res;
    }

    public <R> R getAsync(Function<List<T>, R> func) {
        List<T> o = this.list;
        if (batchSize <= 0 || CollUtil.size(o) <= 0) {
            return func.apply(o);
        }
        R res = batchExecuteAsync(o, func, batchSize);
        tryThrow();
        return res;
    }

    private void tryThrow(){
        if (errorList.isEmpty()) {
            return;
        }

        BatchExecuteException ex = new BatchExecuteException((List) errorList, errEx);
        if (doCatchErr != null) {
            doCatchErr.accept(ex);
        } else if (doCatchList != null) {
            doCatchList.accept(errorList, errEx);
        } else {
            throw ex;
        }
    }


    private <R> R batchExecute(List<T> list, Function<List<T>, R> func, Integer batchSize) {
        R res = null;
        int retry = Math.min(this.retry, 5);
        //获取参数数组
        List<List<T>> pages = CollUtils.partitionList(list, batchSize);
        for (List<T> partition : pages) {
            R partRes = partExecute(partition, func, retry);
            // 避免入参和返回值指向同一个对象
            if (partition == partRes) {
                partRes = (R) new ArrayList<>(partition);
            }
            res = ObjectUtil.merge(res, partRes);
        }
        return res;
    }


    private <R> R batchExecuteAsync(List<T> list, Function<List<T>, R> func, Integer batchSize) {
        Executor executor = this.executor;
        if (executor == null) {
            executor = ThreadUtils.COMMON_EXECUTOR;
        }
        R res = null;
        int retry = Math.min(this.retry, 5);
        //获取参数数组
        List<List<T>> pages = CollUtils.partitionList(list, batchSize);
        List<CompletableFuture<R>> futures = new ArrayList<>();
        for (List<T> partition : pages) {
            CompletableFuture<R> future = CompletableFuture.supplyAsync(() -> partExecute(partition, func, retry), executor);
            futures.add(future);
        }

        try {
            List<R> rs = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                    .get();
            for (R partRes : rs) {
                res = ObjectUtil.merge(res, partRes);
            }
        } catch (Exception e) {
            throw ExceptionUtil.wrapRuntime(e);
        }
        return res;
    }

    private final Transactions trans = Transactions.of().propagate(TransactionDefinition.PROPAGATION_NESTED);


    private <R> R partExecute(List<T> part, Function<List<T>, R> func, int retry) {
        R res = null;
        try {
            if (useTrans) {
                res = trans.get(() -> func.apply(part));
            } else {
                res = func.apply(part);
            }
        } catch (Exception e) {
            if (!useRetry || !useTrans) {
                throw e;
            }
            /*不再重试*/
            if (errorCount.incrementAndGet() >= maxErrorCount || retry == 0 || part.size() == 1) {
                errorList.add(part);
                errEx.add(e);
                log.error("", e);
                return null;
            }
            log.error("执行任务出错 , 开始重试  , 剩余次数 = {} , msg = {} , 当前 list = {}", retry, e.getMessage(), part);
            int pageSize = Integer.max(part.size() / retryDiv, 1);
            List<List<T>> parts = ListUtil.partition(part, pageSize);
            for (List<T> partition : parts) {
                R partRes = partExecute(partition, func, retry - 1);
                if (partRes == partition) { // 避免入参和返回值指向同一个对象
                    partRes = (R) new ArrayList<>(partition);
                }
                res = ObjectUtil.merge(res, partRes);
            }
        }
        return res;
    }

    public  BatchExecutor<T> noRollbackFor(Class<? extends Exception> ex) {
        trans.noRollbackFor(ex);
        return this;
    }


}