package com.littlearphone.mini.arco.functoin;

@FunctionalInterface
public interface CheckedConsumer<T> {
    void accept(T t) throws Throwable;
}
