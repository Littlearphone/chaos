package com.littlearphone.fx.codec;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.Objects;

import static com.littlearphone.fx.constant.Symbol.DOT;
import static com.littlearphone.fx.utils.IOExtend.MP4_SUFFIX;
import static com.littlearphone.fx.utils.UUIDExtend.uuid;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.move;
import static java.nio.file.Files.notExists;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class AbstractCodec implements VideoCodec {
    private static final String READ_MODE = "r";
    @Value("${fx.ffmpeg.executor}")
    protected String executorPath;
    
    @Override
    @SneakyThrows
    public boolean testRestructure(Path path) {
        final String pathString = path.toString();
        if (endsWithIgnoreCase(pathString, MP4_SUFFIX)) {
            return false;
        }
        @Cleanup RandomAccessFile file = new RandomAccessFile(pathString, READ_MODE);
        final int length = readInt(file);
        if (length <= 0) {
            return false;
        }
        String segment = readString(file);
        if (!segment.equals("ftyp")) {
            return false;
        }
        file.skipBytes(length - 2 * 4);
        if (readInt(file) <= 0) {
            return false;
        }
        return !Objects.equals(readString(file), "moov");
    }
    
    @Override
    @SneakyThrows
    public void restructure(Path source) {
        if (isBlank(executorPath) || isNull(source) || notExists(source)) {
            return;
        }
        // ffprobe -v quiet -print_format json -show_format -show_streams
        final ProcessBuilder builder = new ProcessBuilder();
        final Path target = source.resolveSibling(uuid() + MP4_SUFFIX);
        builder.command(
            executorPath,
            "-i", format("\"%s\"", source),
            "-movflags", "faststart",
            "-c:a", "copy",
            "-c:v", "copy",
            format("\"%s\"", target)
        );
        builder.redirectErrorStream(true);
        final Process process = builder.start();
        @Cleanup final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        for (String line = reader.readLine(); nonNull(line); line = reader.readLine()) {
            System.out.println(line);
        }
        if (exists(target)) {
            move(source, toBackup(source), REPLACE_EXISTING);
        }
        move(target, source, REPLACE_EXISTING);
    }
    
    private Path toBackup(final Path source) {
        final String original = source.getFileName().toString();
        final String timestamp = now().format(ofPattern("yyyyMMddHHmmss"));
        return source.resolveSibling(original + DOT + timestamp);
    }
    
    private String readString(RandomAccessFile file) throws IOException {
        final byte[] _4Bit = new byte[4];
        if (file.read(_4Bit) < 4) {
            return EMPTY;
        }
        return new String(_4Bit);
    }
    
    private int readInt(RandomAccessFile stream) throws IOException {
        final byte[] _4Bit = new byte[4];
        if (stream.read(_4Bit) < 4) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < _4Bit.length; i++) {
            result += (_4Bit[i] & 0xff) << ((3 - i) * 8);
        }
        return result;
    }
}
