package com.littlearphone.arco.converter.constant;

import lombok.Getter;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@Getter
public enum VideoType {
    REBUILD("/mp4"),
    CONVERT("/vnd.rn-realmedia"),
    COPY("/x-msvideo", "/quicktime"),
    OTHERS("/octet-stream") // "/x-matroska"
    ;
    private final Set<String> types;
    
    VideoType(String... types) {
        this.types = stream(types).collect(toSet());
    }
}
