package com.littlearphone.arco.converter.utility;

import com.littlearphone.arco.converter.utility.function.CheckedBiFunction;
import com.littlearphone.arco.converter.utility.function.CheckedConsumer;
import com.littlearphone.arco.converter.utility.function.CheckedFunction;
import com.littlearphone.arco.converter.utility.function.CheckedSupplier;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ObjectExtend {

    public static <T> T nullToDefault(T t, T defaultValue) {
        return isNull(t) ? defaultValue : t;
    }

    @SneakyThrows
    public static <T, R> R nullOrConvert(T t, CheckedFunction<T, R> function) {
        return isNull(t) ? null : function.apply(t);
    }

    @SneakyThrows
    public static <T, R> R nullOrConvert(T t, R defaultValue, CheckedFunction<T, R> function) {
        return isNull(t) ? defaultValue : function.apply(t);
    }

    public static <T> void sneakyConsume(T t, CheckedConsumer<T> consumer) {
        try {
            consumer.accept(t);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T> T sneakyConvert(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T, R> R sneakyConvert(T t, CheckedFunction<T, R> function) {
        try {
            return function.apply(t);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T, K, R> R sneakyBiConvert(T t, K k, CheckedBiFunction<T, K, R> function) {
        try {
            return function.apply(t, k);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T, R> R capturedConvert(T t, CheckedFunction<T, R> function) {
        try {
            return function.apply(t);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
