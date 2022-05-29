package com.littlearphone.fx.dto;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributes;

import static com.littlearphone.fx.constant.Keyword.FILE;
import static com.littlearphone.fx.constant.Keyword.FOLDER;
import static com.littlearphone.fx.constant.Symbol.DOT;
import static java.lang.Long.parseLong;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.readAttributes;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.lowerCase;

@Data
@Slf4j
@Accessors(chain = true)
public class FileDto {
    private static final String SPACE = " ";
    private static final String COMMA = ",";
    private static final String BACKSLASH = "\\";
    private String name;
    private String type;
    private String path;
    private long length;
    private boolean link;
    private boolean hidden;
    private String lastModified;
    
    public static FileDto from(String line, String parent) {
        final StringBuilder builder = new StringBuilder(line);
        final String size = spaceSplit(builder);
        final String attr = spaceSplit(builder);
        final String date = spaceSplit(builder);
        final String time = spaceSplit(builder);
        final String name = builder.substring(builder.lastIndexOf(BACKSLASH) + 1);
        final String path = parent + BACKSLASH + name;
        try {
            Path of = Path.of(path);
            return new FileDto()
                .setPath(path)
                .setName(name)
                .setType(detectType(of))
                .setLink(isSymbolicLink(of))
                .setHidden(attr.contains("H"))
                .setLastModified(date + SPACE + time)
                .setLength(parseLong(size.replace(COMMA, EMPTY)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    private static String spaceSplit(final StringBuilder builder) {
        final int sizeIndex = builder.indexOf(SPACE);
        try {
            return builder.substring(0, sizeIndex);
        } finally {
            builder.delete(0, sizeIndex);
        }
    }
    
    public static FileDto from(Path path, Path base) {
        try {
            return new FileDto()
                .setLink(isSymbolicLink(path))
                .setHidden(isHidden(path))
                .setPath(base.relativize(path.toAbsolutePath()).toString())
                .setName(path.getFileName().toString())
                .setLastModified(lastModified(path))
                .setType(detectType(path))
                .setLength(size(path));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    private static boolean isSymbolicLink(Path path) throws Exception {
        return Files.isSymbolicLink(path) || !path.toRealPath().toString().equals(path.toString());
    }
    
    private static boolean isHidden(Path path) throws Exception {
        return Files.isHidden(path) || path.toFile().isHidden()
            || readAttributes(path, DosFileAttributes.class, NOFOLLOW_LINKS).isHidden();
    }
    
    private static String detectType(final Path path) {
        if (isDirectory(path)) {
            return FOLDER;
        }
        // if (!Files.isReadable(path)) {
        //     return "occupied";
        // }
        final String name = path.getFileName().toString();
        final int dotIndex = name.lastIndexOf(DOT);
        if (dotIndex < 0) {
            return FILE;
        }
        return lowerCase(name.substring(dotIndex + 1));
        // try {
        //     return upperCase(TIKA.detect(path));
        // } catch (FileSystemException | TaggedIOException e) {
        //     log.error(path.toString());
        //     log.error(e.getMessage());
        // }
        // return "occupied";
    }
    
    @SneakyThrows
    private static String lastModified(Path path) {
        return getLastModifiedTime(path).toInstant().atZone(systemDefault()).format(ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    @SneakyThrows
    private static long size(Path path) {
        return Files.size(path);
    }
    
}
