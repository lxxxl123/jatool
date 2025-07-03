package com.chen.jatool.common.utils.support.lambda;


import java.io.Serializable;
import java.util.function.Function;

@FunctionalInterface
public interface Func1<P1, R> extends Serializable, Function<P1, R> {
    R apply(P1 t);
}
