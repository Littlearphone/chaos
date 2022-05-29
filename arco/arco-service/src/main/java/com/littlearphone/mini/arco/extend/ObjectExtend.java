package com.littlearphone.mini.arco.extend;

import com.littlearphone.mini.arco.functoin.CheckedBiFunction;
import com.littlearphone.mini.arco.functoin.CheckedConsumer;
import com.littlearphone.mini.arco.functoin.CheckedFunction;
import com.littlearphone.mini.arco.functoin.CheckedSupplier;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class ObjectExtend {

    public static <T> T nullToDefault(T t, T defaultValue) {
        return isNull(t) ? defaultValue : t;
    }


    public static <T, R> R nullOrConvert(T t, CheckedFunction<T, R> function) {
        return isNull(t) ? null : sneakyConvert(t, function);
    }

    public static <T> void sneakyConsume(T t, CheckedConsumer<T> consumer) {
        try {
            consumer.accept(t);
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T> T sneakyConvert(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T, R> R sneakyConvert(T t, CheckedFunction<T, R> function) {
        try {
            return function.apply(t);
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T, K, R> R sneakyBiConvert(T t, K k, CheckedBiFunction<T, K, R> function) {
        try {
            return function.apply(t, k);
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static <T, R> R capturedConvert(T t, CheckedFunction<T, R> function) {
        try {
            return function.apply(t);
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
