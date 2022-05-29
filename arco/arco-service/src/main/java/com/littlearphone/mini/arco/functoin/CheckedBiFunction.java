package com.littlearphone.mini.arco.functoin;

@FunctionalInterface
public interface CheckedBiFunction<T, K, R> {
    R apply(T t, K k) throws Throwable;
}
