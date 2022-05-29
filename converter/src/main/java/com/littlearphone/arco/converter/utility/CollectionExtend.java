package com.littlearphone.arco.converter.utility;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Stream.empty;
import static java.util.stream.StreamSupport.stream;

public final class CollectionExtend {
    @SafeVarargs
    public static <T> Stream<T> asStream(T... arrays) {
        if (isNull(arrays)) {
            return empty();
        }
        return Stream.of(arrays);
    }

    public static <T> Stream<T> asStream(Iterable<T> iterable) {
        if (isNull(iterable)) {
            return empty();
        }
        return stream(iterable.spliterator(), false);
    }

    public static <T> Stream<T> asStream(Collection<T> collection) {
        if (isNull(collection)) {
            return empty();
        }
        return collection.stream();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return isNull(collection) || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return nonNull(collection) && !collection.isEmpty();
    }
}
