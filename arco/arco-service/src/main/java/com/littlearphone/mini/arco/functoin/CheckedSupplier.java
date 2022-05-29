package com.littlearphone.mini.arco.functoin;

@FunctionalInterface
public interface CheckedSupplier<R> {
    R get() throws Throwable;
}
