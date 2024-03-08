package com.chen.jatool.common.utils.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.chen.jatool.common.exception.BatchExecuteException;
import com.chen.jatool.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 作用: 分批执行 ; 遇到错误自动拆分继续执行
 * @author chenwh3
 */
@Slf4j
public class BatchExecutor<T,R> {
    private final Integer batchSize;

    /**
     * 常用于插入事务
     */
    private boolean nestedTrans = false;
    private int retry = 3;

    private int retryDiv = 5;

    private int maxErrorCount = 50;

    private int errorCount = 0;

    private final Function<List<T>, R> func;

    private final List<T> list;

    public BatchExecutor(List<T> list, Function<List<T>, R> func, Integer batchSize, boolean partTrans, int retry, int retryDiv, int maxErrorCount) {
        this.func = func;
        this.list = list;
        this.batchSize = batchSize;
        this.nestedTrans = partTrans;
        this.retry = retry;
        this.retryDiv = retryDiv;
        this.maxErrorCount = maxErrorCount;
    }

    public BatchExecutor(List<T> list, Function<List<T>, R> func, Integer batchSize) {
        this.func = func;
        this.list = list;
        this.batchSize = batchSize;
    }

    public static <T,R> BatchExecutor<T, R> get(List<T> list, Function<List<T>, R> func, Integer batchSize) {
        return new BatchExecutor<>(list, func, batchSize);
    }

    public static <T> BatchExecutor<T, Void> run(List<T> list, Consumer<List<T>> func, Integer batchSize) {
        return new BatchExecutor<>(list, (e) -> {
            func.accept(e);
            return null;
        }, batchSize);
    }


    public BatchExecutor<T,R> nestedTrans(boolean nestedTrans) {
        this.nestedTrans = nestedTrans;
        return this;
    }


    private final List<Object> errorList = new ArrayList<>();
    private final List<String> errMsg = new ArrayList<>();

    private R executeMethod(List<T> obj) {
        return func.apply(obj);
    }

    @SuppressWarnings("unchecked")
    private static <R> R merge(R o1, R o2) {
        if (o1 == null) {
            return o2;
        }
        if (o2 == null) {
            return o1;
        }
        if (o1 instanceof Collection && o2 instanceof Collection) {
            Collection c1 = ((Collection<?>) o1);
            Collection c2 = ((Collection<?>) o2);
            if (c1.size() > c2.size()) {
                c1.addAll(c2);
                return (R)c1;
            } else {
                c2.addAll(c1);
                return (R) c2;
            }
        }
        if (o1 instanceof Integer && o2 instanceof Integer) {
            return (R) (Object)((Integer) o1 + (Integer) o2);
        }
        if (o1 instanceof Long && o2 instanceof Long) {
            return (R) (Object)((Long) o1 + (Long) o2);
        }
        return null;
    }


    public R execute() {
        List<T> o = this.list;
        if (batchSize <= 0 || CollUtil.size(o) <= 0)  {
            executeMethod(o);
        }
        R res = batchExecute(o, batchSize);
        if (!errorList.isEmpty()) {
            throw new BatchExecuteException(errorList, errMsg);
        }
        return res;
    }

    private final Transations transations = Transations.of();

    private R batchExecute(List<T> list, Integer batchSize) {
        R res = null;
        int retry = Math.min(this.retry, 5);
        //获取参数数组
        List<List<T>> pages = CollUtils.partitionList(list, batchSize);
        for (List<T> partition : pages) {
            R partRes = partExecute(partition, retry);
            // 避免入参和返回值指向同一个对象
            if (partition == partRes) {
                partRes = (R) new ArrayList<>(partition);
            }
            res = merge(res, partRes);
        }
        return res;
    }

    private R partExecute(List<T> part,  int retry)  {
        if(nestedTrans){
            transations.propagate(TransactionDefinition.PROPAGATION_REQUIRES_NEW).begin();
        }
        R res = null;
        try {
            res = merge(executeMethod(part), res);
            transations.tryCommit();
        } catch (Exception e) {
            if (nestedTrans) {
                transations.tryRollback();
            } else {
                throw e;
            }
            /*不再重试*/
            if (retry == 0 || part.size() == 1) {
                errorList.addAll(part);
                if(errorCount++ >= maxErrorCount){
                    throw new BatchExecuteException(errorList, errMsg);
                }
                //避免字数太大
                errMsg.add(e.getLocalizedMessage());
                log.error("", e);
                return null;
            }
            log.error("执行任务出错 , 开始重试  , 剩余次数 = {} , msg = {} , 当前 list = {}", retry, e.getMessage(), part);
            int pageSize = Integer.max(part.size() / retryDiv, 1);
            List<List<T>> parts = ListUtil.partition(part, pageSize);
            for (List<T> partition : parts) {
                R partRes = partExecute(partition, retry - 1);
                if (partRes == partition) { // 避免入参和返回值指向同一个对象
                    partRes = (R) new ArrayList<>(partition);
                }
                res = merge(res, partRes);
            }
        }
        return res;
    }

}