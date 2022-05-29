package com.littlearphone.mini.arco.functoin;

@FunctionalInterface
public interface CheckedFunction<T, R> {
    R apply(T t) throws Throwable;
}
