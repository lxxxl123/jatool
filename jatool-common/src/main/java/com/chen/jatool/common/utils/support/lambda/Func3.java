package com.chen.jatool.common.utils.support.lambda;


import java.io.Serializable;

@FunctionalInterface
public interface Func3<P1, P2,P3, R> extends Serializable {
    R apply(P1 t, P2 u, P3 v);
}
