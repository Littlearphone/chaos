package com.littlearphone.arco.converter.utility.function;

@FunctionalInterface
public interface CheckedBiFunction<T, K, R> {
    R apply(T t, K k) throws Throwable;
}
