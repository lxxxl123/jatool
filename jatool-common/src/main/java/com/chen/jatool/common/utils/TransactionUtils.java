package com.haday.qms.core.tool.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    public static  <T> T executeWithoutTrans(Supplier<T> supplier) {
        return executeTrans(supplier, TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
    }

    public static  <T> T executeInNewTrans(Supplier<T> supplier) {
        return executeTrans(supplier, TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public static <T> T executeTrans(Supplier<T> supplier, int propagation) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(propagation);
        return transactionTemplate.execute(status -> supplier.get());
    }

    public static boolean isInTrans(){
        return TransactionSynchronizationManager.isActualTransactionActive();
    }


}
