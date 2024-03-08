package com.chen.jatool.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * @author chenwh3
 */
@Component
public class TransactionUtils {

    private static PlatformTransactionManager transactionManager;

    @Autowired
    public void init(PlatformTransactionManager transactionManager){
        TransactionUtils.transactionManager = transactionManager;
    }

    public static TransactionStatus getTransaction(TransactionDefinition definition){
        return transactionManager.getTransaction(definition);
    }

    public static void commit(TransactionStatus status){
        transactionManager.commit(status);
    }

    public static void rollback(TransactionStatus status){
        transactionManager.rollback(status);
    }

    /**
     * 一般用于在事务中挂起原来的事务并切换数据源，因事务中无法切换数据源
     */
    public static  <T> T executeWithoutTransaction(Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        return transactionTemplate.execute(status -> supplier.get());
    }

    /**
     *
     */
    public static  <T> T execute(Supplier<T> supplier) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(status -> supplier.get());
    }
}
