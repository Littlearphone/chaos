package com.littlearphone.arco.converter;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.littlearphone.arco.converter.constant.VideoType.CONVERT;
import static com.littlearphone.arco.converter.constant.VideoType.NO_ACTION;
import static com.littlearphone.arco.converter.constant.VideoType.OTHERS;
import static com.littlearphone.arco.converter.constant.VideoType.REBUILD;
import static com.littlearphone.arco.converter.utility.CollectionExtend.asStream;
import static com.littlearphone.arco.converter.utility.PathExtend.FFMPEG;
import static com.littlearphone.arco.converter.utility.PathExtend.JAR_FOLDER;
import static com.littlearphone.arco.converter.utility.PathExtend.LINE_SEPARATOR;
import static com.littlearphone.arco.converter.utility.PathExtend.OS_NAME;
import static com.littlearphone.arco.converter.utility.PathExtend.TEMP_DIR;
import static com.littlearphone.arco.converter.utility.UUIDExtend.uuid;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.move;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.writeString;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.time.Duration.ofMillis;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ArrayUtils.add;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.tika.io.IOUtils.copy;

public class Converter {
    private static final Tika TIKA = new Tika();
    private static final byte[] _4BIT = new byte[4];
    private static final AtomicInteger COUNT = new AtomicInteger();
    private static final Path OUTPUT = get(TEMP_DIR).resolve(uuid()).toAbsolutePath();

    static {
        getRuntime().addShutdownHook(new Thread(Converter::cleanFiles));
        try (
            final InputStream inputStream = Main.class.getResourceAsStream(FFMPEG);
            final FileOutputStream output = new FileOutputStream(OUTPUT.toFile())
        ) {
            copy(requireNonNull(inputStream), output);
            if (OS_NAME.equalsIgnoreCase("linux")) {
                getRuntime().exec("chmod 777 -R " + OUTPUT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private static void cleanFiles() {
        deleteIfExists(OUTPUT);
    }

    @SneakyThrows
    public static List<Object> convertVideo(Path path) {
        final String type = detect(path);
        String fileName = path.getFileName().toString();
        List<Object> data = new ArrayList<>();
        data.add(COUNT.incrementAndGet());
        data.add(type);
        data.add("YES");
        data.add("--");
        data.add(fileName);
        if (asStream(REBUILD.getTypes()).anyMatch(type::endsWith)) {
            if (needRestructure(path)) {
                data.set(3, directStreamCopy(path));
                return data;
            }
            data.set(2, "NO");
            return data;
        }
        if (asStream(CONVERT.getTypes()).anyMatch(type::endsWith)) {
            data.set(3, directStreamCopy(path));
            return data;
        }
        if (asStream(OTHERS.getTypes()).anyMatch(type::endsWith) && endsWithIgnoreCase(fileName, ".ts")) {
            data.set(3, directStreamCopy(path));
            return data;
        }
        data.set(2, "NO");
        if (NO_ACTION.getTypes().contains(type)) {
            // realStreamConvert(path);
        }
        return data;
    }

    @SneakyThrows
    public static String detect(Path path) {
        return lowerCase(TIKA.detect(path));
    }

    @SneakyThrows
    public static boolean needRestructure(Path path) {
        @Cleanup RandomAccessFile file = new RandomAccessFile(path.toString(), "r");
        final int length = readInt(file);
        if (length <= 0) {
            return false;
        }
        String segment = readString(file);
        if (!segment.equals("ftyp")) {
            return false;
        }
        file.skipBytes(length - _4BIT.length - _4BIT.length);
        if (readInt(file) <= 0) {
            return false;
        }
        return !Objects.equals(readString(file), "moov");
    }

    // private static void checkSuffix(Path path, String suffix) {
    //     String source = path.toString();
    //     if (endsWithIgnoreCase(source, suffix)) {
    //         return;
    //     }
    //     System.out.println("Error extension of " + suffix + " file: " + path.getFileName());
    //     source = source.substring(0, source.lastIndexOf(".")) + suffix;
    //     sneakyBiConvert(path, get(source), Files::move);
    // }
    //
    private static String readString(RandomAccessFile file) throws IOException {
        if (file.read(_4BIT) < 4) {
            return "";
        }
        return new String(_4BIT);
    }

    private static int readInt(RandomAccessFile stream) throws IOException {
        if (stream.read(_4BIT) < 4) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < _4BIT.length; i++) {
            result += (_4BIT[i] & 0xff) << ((3 - i) * 8);
        }
        return result;
    }

    // private static void realStreamConvert(Path video) {
    //     ffmpegExecute(video, OUTPUT.toString(),
    //         "-i", video.toString(),
    //         "-movflags", "faststart",
    //         "-qscale", "0",
    //         "-vcodec", "libx264");
    // }
    //
    private static String directStreamCopy(Path video) {
        double cost = ffmpegExecute(video, OUTPUT.toString(),
                                    "-i", format("\"%s\"", video),
                                    "-movflags", "faststart",
                                    "-vcodec", "copy",
                                    "-acodec", "copy");
        return cost >= 0 ? String.valueOf(cost) : "--";
    }

    private static double ffmpegExecute(final Path video, final String... commands) {
        final String outputSuffix = ".mp4";
        final Path parent = video.getParent();
        final Path target = parent.resolve(uuid() + outputSuffix);
        final long start = currentTimeMillis();
        try {
            final Process process = new ProcessBuilder()
                .redirectErrorStream(true)
                .command(add(commands, target.toString()))
                .start();
            if (process.waitFor() != 0) {
                outputLogFile(process);
                return -1;
            }
            final String fullName = video.getFileName().toString();
            final String name = fullName.substring(0, fullName.lastIndexOf("."));
            move(target, parent.resolve(name + outputSuffix), REPLACE_EXISTING);
            deleteIfExists(video);
            return ofMillis(currentTimeMillis() - start).toSeconds();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void outputLogFile(Process process) throws IOException {
        try (final InputStream stream = process.getInputStream()) {
            Path logFile = JAR_FOLDER.resolve("convert.log");
            if (notExists(logFile)) {
                createFile(logFile);
            }
            writeString(logFile, LINE_SEPARATOR + IOUtils.toString(stream) + LINE_SEPARATOR, APPEND);
        }
    }
}
