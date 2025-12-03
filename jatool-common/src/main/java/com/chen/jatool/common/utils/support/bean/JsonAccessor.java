package com.chen.jatool.common.utils.support.bean;

import com.chen.jatool.common.exception.ServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenwh3
 */
public class JsonAccessor extends ObjectAccessor {

    private Map target;

    JsonAccessor(Object target) {
        if (target == null) {
            target = new HashMap<String, Object>();
        }
        if (!(target instanceof Map)) {
            throw new ServiceException("not support type = {}", target.getClass());
        }
        this.target = (Map) target;
    }


    @Override
    public void set(String name, Object value) {
        target.put(name, value);
    }

    @Override
    public Object get(String name) {
        return target.get(name);
    }

    @Override
    public List<String> keys() {
        return new ArrayList<String>(target.keySet());
    }




}
