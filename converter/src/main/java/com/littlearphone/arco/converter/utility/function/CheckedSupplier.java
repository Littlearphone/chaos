package com.littlearphone.arco.converter.utility.function;

@FunctionalInterface
public interface CheckedSupplier<R> {
    R get() throws Throwable;
}
