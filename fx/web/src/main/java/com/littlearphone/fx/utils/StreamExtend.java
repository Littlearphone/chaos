package com.littlearphone.fx.utils;

import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.stream.Stream.empty;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class StreamExtend {
    public static <T> Stream<T> asStream(Collection<T> collection) {
        return isNull(collection) ? empty() : collection.stream();
    }
    
    public static <K, V> Stream<Entry<K, V>> asStream(Map<K, V> map) {
        return isNull(map) ? empty() : map.entrySet().stream();
    }
}
