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
     * 常用于插入事务
     */
    private boolean nestedTrans = false;
    private int retry = 3;

    private int retryDiv = 5;

    private int maxErrorCount = 50;

    private AtomicInteger errorCount = new AtomicInteger(0);

    private final List<Class<? extends Exception>> noRollbackExs = new ArrayList<>();

    private final List<T> list;

    private Executor executor;

    public BatchExecutor(List<T> list, Integer batchSize, boolean partTrans, int retry, int retryDiv, int maxErrorCount) {
        this.list = list;
        this.batchSize = batchSize;
        this.nestedTrans = partTrans;
        this.retry = retry;
        this.retryDiv = retryDiv;
        this.maxErrorCount = maxErrorCount;
    }

    public BatchExecutor(List<T> list, Integer batchSize) {
        this.list = list;
        this.batchSize = batchSize;
    }

    public static <T> BatchExecutor<T> of(List<T> list, int size) {
        return new BatchExecutor<>(list, size);
    }


    public BatchExecutor<T> nestedTrans(boolean nestedTrans) {
        this.nestedTrans = nestedTrans;
        return this;
    }

    public BatchExecutor<T> setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    private Consumer<BatchExecuteException> doWhenError;

    public BatchExecutor<T> catchError(Consumer<BatchExecuteException> consumer) {
        doWhenError = consumer;
        return this;
    }

    private final List<Object> errorList = new CopyOnWriteArrayList<>();
    private final List<String> errMsg = new CopyOnWriteArrayList<>();


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
        if (!errorList.isEmpty()) {
            BatchExecuteException ex = new BatchExecuteException(errorList, errMsg);
            if (doWhenError != null) {
                doWhenError.accept(ex);
            } else {
                throw ex;
            }
        }
        return res;
    }

    public <R> R getAsync(Function<List<T>, R> func) {
        List<T> o = this.list;
        if (batchSize <= 0 || CollUtil.size(o) <= 0) {
            return func.apply(o);
        }
        R res = batchExecuteAsync(o, func, batchSize);
        if (!errorList.isEmpty()) {
            BatchExecuteException ex = new BatchExecuteException(errorList, errMsg);
            if (doWhenError != null) {
                doWhenError.accept(ex);
            } else {
                throw ex;
            }
        }
        return res;
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


    private <R> R partExecute(List<T> part, Function<List<T>, R> func, int retry) {
        Transactions trans = Transactions.of();
        if (nestedTrans) {
            trans = trans.propagate(TransactionDefinition.PROPAGATION_REQUIRES_NEW).begin();

        }
        R res = null;
        try {
            res = ObjectUtil.merge(func.apply(part), res);
            trans.tryCommit();
        } catch (Exception e) {
            boolean skip = noRollbackExs.stream().anyMatch(ex -> ex.isInstance(e));
            if (nestedTrans && !skip) {
                trans.tryRollback();
            } else {
                throw e;
            }
            /*不再重试*/
            if (retry == 0 || part.size() == 1) {
                errorList.addAll(part);
                //避免字数太大
                errMsg.add(e.getLocalizedMessage());
                if (errorCount.incrementAndGet() >= maxErrorCount) {
                    throw (BatchExecuteException) new BatchExecuteException(errorList, errMsg).initCause(e);
                }
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
        noRollbackExs.add(ex);
        return this;
    }


}