package com.chen.jatool.common.utils.support.lambda;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.chen.jatool.common.utils.SpringUtils;

import java.lang.invoke.SerializedLambda;

public class LambdaUtils {
    private static final WeakConcurrentMap<String, SerializedLambda> cache = new WeakConcurrentMap<>();

    public static SerializedLambda resolve(Object func) {
        return _resolve(func);
    }


    /**
     * 注意，以下方法入参必须继承 Serializable 接口
     * 如：Func0 , Func1等
     */
    public static <T> Object getSpringBean(Object func){
        String implClass = resolve(func).getImplClass();
        String simpleClassName = implClass.substring(implClass.lastIndexOf("/") + 1);
        return SpringUtils.getBean(StrUtil.lowerFirst(simpleClassName));
    }

    public static String getMethodName(Object func) {
        return resolve(func).getImplMethodName();
    }

    public static String getFieldName(Object func) {
        return BeanUtil.getFieldName(getMethodName(func));
    }

    private static SerializedLambda _resolve(Object func) {
        return cache.computeIfAbsent(func.getClass().getName(), (key) -> ReflectUtil.invoke(func, "writeReplace"));
    }


}
