package com.littlearphone.arco.converter.utility;

import lombok.SneakyThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public final class PathExtend {
    public static final String LINE_SEPARATOR = getProperty("line.separator");
    public static final String OS_NAME = getProperty("os.name");
    public static final String TEMP_DIR = getProperty("java.io.tmpdir");
    public static final String FFMPEG = chooseLibrary();
    public static final Path JAR_FOLDER = jarFolder();

    private static String chooseLibrary() {
        if (equalsIgnoreCase(OS_NAME, "linux")) {
            return "/codec/" + OS_NAME.toLowerCase() + "/ffmpeg";
        }
        return "/codec/windows/ffmpeg";
    }

    @SneakyThrows
    private static Path jarFolder() {
        // jar:file:/home/deepin/Downloads/Video/converter-0.0.1-SNAPSHOT.jar!/BOOT-INF/classes!/
        String dirtyPath = PathExtend.class.getResource("").toString();
        String jarPath = dirtyPath.replaceAll("^.*file:/", "");
        jarPath = jarPath.replaceAll("jar!.*", "jar");
        jarPath = jarPath.replaceAll("%20", " ");
        if (!jarPath.endsWith(".jar")) {
            jarPath = jarPath.replaceAll("/classes/.*", "/classes/");
        }
        return Paths.get(jarPath).getParent();
    }
}
