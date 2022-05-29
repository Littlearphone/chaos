package com.littlearphone.fx.codec;

import java.nio.file.Path;

public interface VideoCodec {
    default boolean testStreaming(Path path) {
        return false;
    }
    
    default boolean testRestructure(Path path) {
        return false;
    }
    
    default void restructure(Path path) {
    }
}
