package com.chen.jatool.common.utils.support.lambda;


import java.io.Serializable;

@FunctionalInterface
public interface Func1<P1, R> extends Serializable {
    R apply(P1 t);
}
