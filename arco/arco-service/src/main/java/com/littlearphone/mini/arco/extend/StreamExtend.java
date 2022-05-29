package com.littlearphone.mini.arco.extend;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class StreamExtend {
    public static <T> Stream<T> asStream(Collection<T> collection) {
        if (isEmpty(collection)) {
            return empty();
        }
        return collection.stream();
    }
}
