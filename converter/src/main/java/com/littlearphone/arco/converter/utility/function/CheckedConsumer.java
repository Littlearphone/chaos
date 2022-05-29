package com.littlearphone.arco.converter.utility.function;

@FunctionalInterface
public interface CheckedConsumer<T> {
    void accept(T t) throws Throwable;
}
