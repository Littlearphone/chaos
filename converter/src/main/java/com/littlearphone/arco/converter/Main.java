package com.littlearphone.arco.converter;

import com.littlearphone.arco.converter.constant.VideoType;
import com.littlearphone.arco.converter.printer.ExtendTablePrinter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.littlearphone.arco.converter.Converter.detect;
import static com.littlearphone.arco.converter.constant.VideoType.values;
import static com.littlearphone.arco.converter.utility.CollectionExtend.asStream;
import static com.littlearphone.arco.converter.utility.ObjectExtend.nullToDefault;
import static com.littlearphone.arco.converter.utility.PathExtend.JAR_FOLDER;
import static java.lang.System.getProperty;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.list;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static me.tongfei.progressbar.ProgressBar.wrap;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class Main {
    private static final String[] HEADERS = {"No.", "File Type", "Convert", "Cost(s)", "File Name"};

    public static void main(String[] args) throws Exception {
        final Object[][] table = doConvert(searchVideos(args));
        new ExtendTablePrinter(HEADERS, table).printTable();
        pressEnterKeyToContinue();
    }

    private static void pressEnterKeyToContinue() {
        System.out.println("Press Enter key to continue...");
        try {
            System.in.read();
        } catch (Exception ignored) {
        }
    }

    private static Object[][] doConvert(List<Path> videos) {
        System.out.printf("Total %s videos%n", videos.size());
        return asStream(wrap(videos, "Start "))
            .map(Converter::convertVideo)
            .map(List::toArray)
            .collect(toList())
            .toArray(Object[][]::new);
    }

    private static List<Path> searchVideos(String[] args) throws IOException {
        if (isEmpty(args)) {
            System.out.println("Not specify video file(s), search current folder");
            return flatVideos(list(JAR_FOLDER).map(Path::toAbsolutePath)).collect(toList());
        }
        return stream(args)
            .distinct()
            .map(Paths::get)
            .filter(Files::exists)
            .flatMap(path -> flatVideos(path, true))
            .filter(path -> {
                final String type = detect(path);
                return asStream(values())
                    .map(VideoType::getTypes)
                    .flatMap(Collection::stream)
                    .anyMatch(type::endsWith);
            })
            .collect(toList());
    }

    private static Stream<Path> flatVideos(Stream<Path> paths) {
        return nullToDefault(paths, Stream.<Path>empty()).flatMap(Main::flatVideos);
    }

    @SneakyThrows
    private static Stream<Path> flatVideos(Path path) {
        return flatVideos(path, isRecursive());
    }

    @SneakyThrows
    private static Stream<Path> flatVideos(Path path, boolean recursive) {
        if (isRegularFile(path)) {
            return Stream.of(path);
        }
        if (recursive) {
            return flatVideos(list(path));
        }
        return empty();
    }

    private static boolean isRecursive() {
        return equalsIgnoreCase(getProperty("recursive"), "true");
    }
}
