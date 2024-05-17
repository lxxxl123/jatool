package com.chen.jatool.common.utils.support;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import com.chen.jatool.common.utils.support.string.MessageBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author chenwh3
 */
@Slf4j
public class Validators<T> {

    private final T obj;

    private MessageBuilder mb;

    public static <T> Validators<T> of(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("obj in validators is null");
        }
        return new Validators<>(obj);
    }

    private Validators(T obj) {
        this.obj = obj;
    }

    public Validators<T> message(String title, String separator, String tail) {
        this.mb = new MessageBuilder(title, separator, tail);
        return this;
    }

    private boolean ifBlank(Object obj) {
       if (ObjectUtil.isBlank(obj)) {
            return true;
        }
        return false;
    }


    public Validators<T> checkIfBlank(Func1<T, Object> func, String msg) {
        try {
            Object call = func.call(obj);
            if (ifBlank(call)) {
                mb.appendln(msg);
            }
        } catch (Exception e) {
            dealError(e);
        }
        return this;
    }

    public Validators<T> setIfBlank(Func1<T, Object> func, Object val) {
        try {
            Object call = func.call(obj);
            if (ifBlank(call)) {
                String fieldName = LambdaUtil.getFieldName(func);
                ObjectUtil.set(obj, fieldName, val);
            }
        } catch (Exception e) {
            dealError(e);
        }
        return this;
    }

    public Validators<T> setIfBlank(Func1<T, Object> func, Supplier<?> val) {
        try {
            Object call = func.call(obj);
            if (ifBlank(call)) {
                String fieldName = LambdaUtil.getFieldName(func);
                ObjectUtil.set(obj, fieldName, val.get());
            }
        } catch (Exception e) {
            dealError(e);
        }
        return this;
    }

    private void dealError(Exception e) {
        if (mb != null) {
            log.error("", e);
            mb.appendln(e.getLocalizedMessage());
        } else {
            throw ExceptionUtil.wrapRuntime(e);
        }

    }

    public Validators<T> doIfBlank(Func1<T, Object> func, Consumer<T> consumer) {
        try {
            Object call = func.call(obj);
            if (ifBlank(call)) {
                consumer.accept(obj);
            }
        } catch (Exception e) {
            dealError(e);
        }
        return this;
    }


    public Validators<T> tryThrow() {
        if (mb != null && mb.containMsg()) {
            throw new ServiceException(mb.toString());
        }
        return this;
    }

}
