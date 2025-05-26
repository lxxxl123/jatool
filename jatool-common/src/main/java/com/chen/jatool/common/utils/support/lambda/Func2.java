package com.chen.jatool.common.utils.support.lambda;


import java.io.Serializable;

@FunctionalInterface
public interface Func2<P1, P2, R> extends Serializable {
    R apply(P1 t, P2 u);
}
