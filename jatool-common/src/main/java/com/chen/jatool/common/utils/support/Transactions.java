package com.chen.jatool.common.utils.support;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.haday.qms.core.tool.utils.TransactionUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author chenwh3
 */
public class Transactions {

    private TransactionStatus transactionStatus;

    private DefaultTransactionDefinition def = new DefaultTransactionDefinition();
    private List<Class<? extends Exception>> rollbackExs = new ArrayList<>();
    private List<Class<? extends Exception>> noRollbackExs = new ArrayList<>();

    public static Transactions of() {
        return new Transactions();
    }

    {
        initDef();
    }

    private void initDef() {
        propagate(TransactionDefinition.PROPAGATION_REQUIRED);
        isolation(TransactionDefinition.ISOLATION_DEFAULT);
        timeout(60);
    }

    /**
     * {@link TransactionDefinition#PROPAGATION_REQUIRED}
     * {@link TransactionDefinition#PROPAGATION_REQUIRES_NEW}
     * {@link TransactionDefinition#PROPAGATION_SUPPORTS}
     * {@link TransactionDefinition#PROPAGATION_NOT_SUPPORTED}
     * {@link TransactionDefinition#PROPAGATION_NEVER}
     * {@link TransactionDefinition#PROPAGATION_MANDATORY}
     * {@link TransactionDefinition#PROPAGATION_NESTED} 局部提交
     * 开启事务
     */
    public Transactions propagate(int propagationBehavior) {
        def.setPropagationBehavior(propagationBehavior);
        return this;
    }

    /**
     * {@link TransactionDefinition#ISOLATION_DEFAULT}
     * {@link TransactionDefinition#ISOLATION_READ_UNCOMMITTED}
     * {@link TransactionDefinition#ISOLATION_READ_COMMITTED}
     * {@link TransactionDefinition#ISOLATION_REPEATABLE_READ}
     */
    public Transactions isolation(int isolationLevel) {
        def.setIsolationLevel(isolationLevel);
        return this;
    }

    public Transactions rollbackFor(Class<? extends Exception> ex) {
        rollbackExs.add(ex);
        return this;
    }

    public Transactions noRollbackFor(Class<? extends Exception> ex) {
        noRollbackExs.add(ex);
        return this;
    }

    /**
     * @param timeout unit: second
     * 超时回滚，但超时不会马上终止任务，会在执行完毕再回滚
     */
    public Transactions timeout(int timeout) {
        def.setTimeout(timeout);
        return this;
    }

    public Transactions begin() {
        this.transactionStatus = TransactionUtils.getTransaction(def);
        return this;
    }

    public void tryCommit() {
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            commit();
        }
    }

    public void tryRollback() {
        if (transactionStatus != null && !transactionStatus.isCompleted()) {
            rollback();
        }
    }

    public void commit() {
        check();
        TransactionUtils.commit(transactionStatus);
    }

    private void check() {
        if (transactionStatus == null) {
            throw new ServiceException("transaction not begin");
        }

        if (transactionStatus.isCompleted()) {
            throw new ServiceException("transaction already completed");
        }

    }

    public void rollback() {
        check();
        TransactionUtils.rollback(transactionStatus);
    }

    public <T> T get(Supplier<T> supplier) {
        begin();
        T res;
        try {
            res = supplier.get();
            commit();
        } catch (Exception e) {
            if (rollbackExs.stream().anyMatch(ex -> ex.isInstance(e))) {
                rollback();
            } else if (noRollbackExs.stream().anyMatch(ex -> ex.isInstance(e))) {
                // do nothing
            } else {
                rollback();
            }
            throw ExceptionUtil.wrapRuntime(e);
        } finally {
            tryCommit();
        }
        return res;
    }

    public void run (Runnable runnable) {
        get(() -> {
            runnable.run();
            return null;
        });
    }

}
