package com.chen.jatool.common.utils.support;

import com.chen.jatool.common.utils.support.lambda.Func1;
import com.chen.jatool.common.utils.support.lambda.LambdaUtils;
import com.chen.jatool.common.utils.support.sql.JdbcTool;
import com.chen.jatool.common.utils.support.sql.PlainWrapper;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author chenwh3
 */
public class QueryProxyEx<T> extends com.haday.qms.core.tool.support.QueryProxy {

    private Class<T> entityClass;

    public QueryProxyEx() {
    }

    public static Class<?> getListType(Method method) {
        return (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
    }

    public static <F, O> QueryProxyEx<O> ofFunEx(Func1<F, List<O>> func1) {
        QueryProxyEx<O> queryProxy = new QueryProxyEx<>();
        queryProxy.entityClass = (Class<O>) getListType(LambdaUtils.getMethod(func1));
        queryProxy.jdbcTool = JdbcTool.ofFunc(func1);
        return queryProxy;
    }

    public List<T> getListByField(Func1<T, ?> field, Object obj) {
        return getListBy((w)->w.tryIn(LambdaUtils.getFieldName(field), obj).blockIfEmpty());
    }

    public List<T> getListBy(Consumer<PlainWrapper> consumer) {
        PlainWrapper wrapper = PlainWrapper.of();
        consumer.accept(wrapper);
        return setWrapper(wrapper.blockIfEmpty()).getList(entityClass);
    }

    public QueryProxyEx<T> computePlainWrapper(Consumer<PlainWrapper> consumer) {
        super.computePlainWrapper(consumer);
        return this;
    }

}
