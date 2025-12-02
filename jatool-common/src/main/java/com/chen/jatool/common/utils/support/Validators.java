package com.chen.jatool.common.utils.support;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.ObjectUtil;
import com.chen.jatool.common.utils.StringUtil;
import com.chen.jatool.common.utils.support.bean.ObjectAccessor;
import com.chen.jatool.common.utils.support.lambda.Func1;
import com.chen.jatool.common.utils.support.lambda.LambdaUtils;
import com.chen.jatool.common.utils.support.string.MessageBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

    private MessageBuilder getMb() {
        if (mb == null) {
            mb = new MessageBuilder();
            mb.setSeparator("\n");
        }
        return mb;
    }

    public Validators<T> message(String title, String separator, String tail) {
        this.mb = new MessageBuilder(title, separator, tail);
        return this;
    }

    public Validators<T> message(Function<T, MessageBuilder> messageBuilderFunction) {
        this.mb = messageBuilderFunction.apply(obj);
        return this;
    }

    private static boolean isBlank(Object obj) {
        return ObjectUtil.isBlank(obj);
    }

    protected <S> Validators<T> checkIsPass(Func1<T, S> func, Func1<S, Boolean> passCondition, String msg) {
        try {
            S val = func.apply(obj);
            if (!passCondition.apply(val)) {
                getMb().appendln(msg);
            }
        } catch (Exception e) {
            dealError(e);
        }
        return this;
    }

    protected Validators<T> checkIsPass(String key, Func1<Object, Boolean> passCondition, String msg) {
        return checkIsPass(e -> ObjectUtil.get(e, key), passCondition, msg);
    }

    public Validators<T> doIf(Func1<T, Object> func, Func1<Object, Boolean> condition, Func1<Object, Object> setVal) {
        try {
            Object call = func.apply(obj);
            if (condition.apply(call)) {
                String fieldName = LambdaUtils.getFieldName(func);
                ObjectUtil.set(obj, fieldName, setVal.apply(call));
            }
        } catch (Exception e) {
            dealError(e);
        }
        return this;
    }

    public Validators<T> checkNotBlank(String key, String msg) {
        return checkIsPass(key, ObjectUtil::isNotBlank, msg);
    }

    public Validators<T> checkNotNull(String key, String msg) {
        return checkIsPass(key, Objects::nonNull, msg);
    }

    public Validators<T> checkNotBlank(Func1<T, Object> func, String msg) {
        return checkIsPass(func, ObjectUtil::isNotBlank, msg);
    }

    public Validators<T> checkIsBlank(Func1<T, Object> func, String msg) {
        return checkIsPass(func, ObjectUtil::isBlank, msg);
    }


    public Validators<T> checkNotNull(Func1<T, Object> func, String msg) {
        return checkIsPass(func, Objects::nonNull, msg);
    }

    public Validators<T> checkTrue(Func1<T, Boolean> func1, String msg) {
        return checkIsPass(e -> e, func1, msg);
    }


    /**
     * 命名有问题，使用checkNotBlank避免歧义
     */
    @Deprecated
    public Validators<T> checkIfBlank(Func1<T, Object> func, String msg) {
        return checkIsPass(func, ObjectUtil::isNotBlank, msg);
    }

    /**
     * 正数
     */
    public Validators<T> checkIsPositiveNum(Func1<T, Object> func, String msg) {
        return checkIsDecimal(func, e -> e.gt(0), msg);
    }

    /**
     * >=0的数字
     */
    public Validators<T> checkIsNonNegativeNum(Func1<T, Object> func, String msg) {
        return checkIsDecimal(func, e -> e.ge(0), msg);
    }

    public Validators<T> checkIsDecimal(Func1<T, Object> func, Predicate<Numbers> condition, String msg) {
        return checkIsPass(func,
                (e) -> {
                    if (isBlank(e)) {
                        return true;
                    }
                    Numbers nb = Numbers.ofEx(e);
                    return nb != null && (condition == null || condition.test(nb));
                }, msg);
    }


    public Validators<T> checkIsDecimal(Func1<T, Object> func, String msg) {
        checkIsDecimal(func, null, msg);
        return this;
    }

    public Validators<T> checkIsNumber(Func1<T, Object> func, String msg) {
        return checkIsPass(func,
                // 空忽略
                val -> isBlank(val) || NumberUtil.isNumber(val.toString().trim())
                , msg);
    }

    public Validators<T> checkIsInteger(Func1<T, Object> func, String msg) {
        return checkIsPass(func,
                // 空忽略
                val -> isBlank(val) || NumberUtil.isInteger(val.toString().trim())
                , msg);
    }


    public Validators<T> checkMaxLength(Func1<T, Object> func, int length, String fieldName) {
        return checkIsPass(func,
                val -> isBlank(val) || StringUtil.getAsciiLength(val.toString()) <= length
                , StrUtil.format("[{}]字符长度不能超过{}", fieldName, length));
    }

    public Validators<T> checkMaxNLength(Func1<T, Object> func, int length, String fieldName) {
        return checkIsPass(func,
                val -> isBlank(val) || val.toString().length() <= length
                , StrUtil.format("[{}]字符长度不能超过{}", fieldName, length));
    }


    public Validators<T> checkMatchRegex(Func1<T, Object> func, String regex, String msg) {
        return checkIsPass(func,
                val -> isBlank(val) || Pattern.matches(regex, val.toString())
                , msg);
    }

    public Validators<T> setIfNull(Func1<T, Object> func, Object val) {
        return doIf(func
                , Objects::isNull
                , e -> val
        );
    }

    public Validators<T> setUpperCase(Func1<T, Object> func) {
        return doIf(func
                , e -> e instanceof String
                , e -> e.toString().toUpperCase()
        );
    }

    public Validators<T> setLowerCase(Func1<T, Object> func) {
        return doIf(func
                , e -> e instanceof String
                , e -> e.toString().toLowerCase()
        );
    }

    public Validators<T> setIfBlank(Func1<T, Object> func, Object val) {
        return doIf(func
                , ObjectUtil::isBlank
                , e -> val
        );
    }

    public Validators<T> setIfBlank(Func1<T, Object> func, Supplier<?> val) {
        return doIf(func
                , ObjectUtil::isBlank
                , e -> val.get()
        );
    }

    public Validators<T> tryTrim(Func1<T, Object> func) {
        return doIf(func
                , e -> e instanceof String
                , e -> e.toString().trim()
        );
    }


    public Validators<T> fixSpace(Func1<T, Object> func) {
        return doIf(func
                , e -> e instanceof String
                , e -> e.toString().replaceAll("\\h", " "));
    }

    /**
     * 包括换行符
     */
    public Validators<T> tryTrimFull() {
        ObjectAccessor sourceAcc = ObjectAccessor.of(obj);
        sourceAcc.keys().forEach(key -> {
            Object val = sourceAcc.get(key);
            if (val instanceof String) {
                sourceAcc.set(key, StrUtil.trim(val.toString()));
            }
        });
        return this;
    }


    public Validators<T> blankToNull(Func1<T, Object> func) {
        return doIf(func
                , ObjectUtil::isBlank
                , e -> null
        );
    }

    static int errorNum = 0;

    private void dealError(Exception e) {
        log.warn("{}", e.getLocalizedMessage());
        if (errorNum++ % 20 == 0) {
            errorNum = 0;
            log.warn("", e);
        }
        log.debug("", e);
        getMb().appendln(e.getLocalizedMessage());
    }


    public Validators<T> tryThrow() {
        if (mb != null && mb.containMsg()) {
            throw new ServiceException(mb.toString());
        }
        return this;
    }

}
