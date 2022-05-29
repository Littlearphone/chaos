package com.littlearphone.mini.arco.extend;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyBiConvert;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConsume;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static com.littlearphone.mini.arco.extend.UuidExtend.uuid;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.nio.file.Files.notExists;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.lowerCase;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class CodecExtend {
    private static final byte[] _4BIT = new byte[4];
    private static final Tika TIKA = new Tika();
    private static final Path CODEC = extractCodec();

    static {
        getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Do mop up");
            if (notExists(CODEC)) {
                return;
            }
            sneakyConvert(CODEC, Files::deleteIfExists);
        }));
    }

    private static Path extractCodec() {
        String platform = lowerCase(getProperty("sun.desktop") + getProperty("sun.arch.data.model"));
        InputStream stream = CodecExtend.class.getResourceAsStream("/codec/" + platform + "/ffmpeg");
        Path path = get(getProperty("java.io.tmpdir")).resolve("ffmpeg");
        sneakyBiConvert(stream, path, (a, b) -> Files.copy(a, b, REPLACE_EXISTING));
        return path;
    }

    public static String typeOf(Path path) {
        return sneakyConvert(path, TIKA::detect);
    }

    @SneakyThrows
    public static boolean needRestructure(Path path) {
        @Cleanup RandomAccessFile file = sneakyBiConvert(path.toString(), "r", RandomAccessFile::new);
        final int length = sneakyBiConvert(file, _4BIT, CodecExtend::readInt);
        if (length <= 0) {
            return false;
        }
        String segment = sneakyBiConvert(file, _4BIT, CodecExtend::readString);
        if (!segment.equals("ftyp")) {
            return false;
        }
        sneakyConsume(length - _4BIT.length - _4BIT.length, file::skipBytes);
        if (sneakyBiConvert(file, _4BIT, CodecExtend::readInt) <= 0) {
            return false;
        }
        return !Objects.equals(sneakyBiConvert(file, _4BIT, CodecExtend::readString), "moov");
    }

    public static String readString(RandomAccessFile file, byte[] bytes) throws IOException {
        final int read = file.read(bytes);
        if (read < 4) {
            return "";
        }
        return new String(bytes);
    }

    public static int readInt(RandomAccessFile stream, byte[] bytes) throws IOException {
        final int read = stream.read(bytes);
        if (read < 4) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result += (bytes[i] & 0xff) << ((3 - i) * 8);
        }
        return result;
    }

    public static String sizeOf(Path path) {
        long fileSize = sneakyConvert(path, Files::size);
        return fileSize / (1024.0 * 1024.0) + "MB";
    }

    public static String nameOf(Path path, String suffix) {
        String realPath = path.toString();
        return realPath.substring(0, realPath.lastIndexOf(".")) + suffix;
    }

    public static void toStreaming(Path path) {
        if (isNull(path) || notExists(path) || !endsWithAny(typeOf(path), "application/mp4", "video/mp4")) {
            return;
        }
        if (!needRestructure(path)) {
            checkSuffix(path, ".mp4");
            return;
        }
        log.info("Unconverted video found: {}", path);
        String realPath = path.toString();
        String tempPath = uuid() + ".mp4";
        String[] commands = new String[]{CODEC.toString(),
            "-i", realPath,
            "-movflags", "faststart",
            "-vcodec", "copy",
            "-acodec", "copy",
            tempPath};
        // if (realPath.endsWith(".wmv")) {
        //     commands = new String[]{CODEC.toString(), "-i", realPath, destination};
        // }
        long startTime = currentTimeMillis();
        ProcessBuilder builder = new ProcessBuilder()
            .redirectErrorStream(true)
            .command(commands);
        Process process = sneakyConvert(builder, ProcessBuilder::start);
        log.info(sneakyConvert(process.getInputStream(), IOUtils::toString));
        log.info("Took {}s finish [{}] conversion", (currentTimeMillis() - startTime) / 1000.0, path);
        sneakyConsume(path, Files::deleteIfExists);
        sneakyBiConvert(get(tempPath), get(nameOf(path, ".mp4")), Files::move);
    }

    public static void checkSuffix(Path path, String suffix) {
        String source = path.toString();
        if (endsWithIgnoreCase(source, suffix)) {
            return;
        }
        log.warn("Error extension of {} file: {}", suffix, path.getFileName());
        source = source.substring(0, source.lastIndexOf(".")) + suffix;
        sneakyBiConvert(path, get(source), Files::move);
    }
}
