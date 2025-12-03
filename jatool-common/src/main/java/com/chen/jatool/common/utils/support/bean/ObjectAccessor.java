package com.chen.jatool.common.utils.support.bean;

import com.chen.jatool.common.exception.ServiceException;
import com.chen.jatool.common.utils.support.bean.BeanAccessor;
import com.chen.jatool.common.utils.support.bean.JsonAccessor;
import com.chen.jatool.common.utils.support.lambda.Func1;

import java.util.List;
import java.util.Map;

/**
 * @author chewh3
 */
public abstract class ObjectAccessor {


    public static ObjectAccessor of(Object target) {
        ObjectAccessor res ;
        if (target instanceof Map) {
            res = new JsonAccessor(target);
        } else {
            res = new BeanAccessor(target);
        }
        return res;
    }

    public abstract void set(String name, Object value) ;

    public abstract Object get(String name);

    public abstract List<String> keys();

    public void setIfAbsent(String name, Object value) {
        if (get(name) == null) {
            set(name, value);
        }
    }

    public <T> T computeIfAbsent(String name, Func1<String, T> func) {
        Object res = get(name);
        if (res == null) {
            set(name, func.apply(name));
        }
        return (T) res;
    }

    public void copyTo(ObjectAccessor target) {
        for(String key : keys()) {
            target.set(key, get(key));
        }
    }
}
