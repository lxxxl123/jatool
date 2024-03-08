package com.chen.jatool.common.utils.support;

import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.TransactionUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author chenwh3
 */
public class Transations {

    private TransactionStatus transactionStatus;

    private DefaultTransactionDefinition def;

    private boolean isCommit = false;

    public static Transations of() {
        return new Transations();
    }

    {
        initDef();
    }

    private void initDef() {
        def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setTimeout(60);
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
    public Transations propagate(int propagationBehavior) {
        def.setPropagationBehavior(propagationBehavior);
        return this;
    }

    /**
     *
     * @param timeout unit: second
     */
    public Transations timeout(int timeout) {
        def.setTimeout(timeout);
        return this;
    }

    public Transations begin() {
        isCommit = false;
        this.transactionStatus = TransactionUtils.getTransaction(def);
        return this;
    }

    public void tryCommit() {
        if(transactionStatus != null) {
            commit();
        }
    }

    public void tryRollback() {
        if(transactionStatus != null) {
            rollback();
        }
    }

    public void commit() {
        check();
        TransactionUtils.commit(transactionStatus);
        isCommit = true;
    }

    private void check() {
        if (isCommit) {
            throw new ServiceException("transaction has been commit");
        }
        if (transactionStatus == null) {
            throw new ServiceException("transaction not begin");
        }

    }

    public void rollback() {
        check();
        TransactionUtils.rollback(transactionStatus);
    }

}
