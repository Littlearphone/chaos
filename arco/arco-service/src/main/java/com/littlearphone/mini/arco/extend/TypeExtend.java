package com.littlearphone.mini.arco.extend;

import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@NoArgsConstructor(access = PRIVATE)
public class TypeExtend {
    public static boolean videoDetect(Path path) {
        String name = path.getFileName().toString();
        return Stream.of(".mp4", ".rm", ".rmvb", ".mkv", ".avi", ".wmv", ".ts")
                     .anyMatch(suffix -> endsWithIgnoreCase(name, suffix));
    }

    public static boolean metaDetect(String name) {
        return name.endsWith(".nfo");
    }
}
