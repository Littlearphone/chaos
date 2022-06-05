package com.littlearphone.arco.converter;

import com.littlearphone.arco.converter.constant.VideoType;
import com.littlearphone.arco.converter.printer.ExtendTablePrinter;
import com.littlearphone.arco.converter.utility.StreamExtend;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.littlearphone.arco.converter.Converter.detect;
import static com.littlearphone.arco.converter.constant.VideoType.values;
import static com.littlearphone.arco.converter.utility.HttpExtend.url;
import static com.littlearphone.arco.converter.utility.JSONExtend.toBean;
import static com.littlearphone.arco.converter.utility.ObjectExtend.nullToDefault;
import static com.littlearphone.arco.converter.utility.PathExtend.JAR_FOLDER;
import static com.littlearphone.arco.converter.utility.StreamExtend.asStream;
import static java.lang.System.getProperty;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.list;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.empty;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public class Main {
    private static final int ENABLE_FEATURE = 1;
    private static final String[] HEADERS = {"No.", "File Type", "Convert", "Cost(s)", "File Name"};
    
    public static void main(String[] args) throws Exception {
        final EverythingSearchResults results = search("111111111111.rmvb");
        final String[] files = results.getResults()
            .stream()
            .map(result -> Paths.get(result.getPath()).resolve(result.getName()))
            .map(Path::toString)
            .toArray(String[]::new);
        final Object[][] table = doConvert(searchVideos(files));
        SECONDS.sleep(1);
        System.out.printf("Total of %s videos were converted%n", table.length);
        System.out.println();
        System.out.flush();
        new ExtendTablePrinter(HEADERS, table).printTable();
        pressEnterKeyToContinue();
    }
    
    private static EverythingSearchResults search(final String pattern) {
        return toBean(url("http://Lenovo:23333")
            .withQuery("path_column", ENABLE_FEATURE)
            .withQuery("json", ENABLE_FEATURE)
            .withQuery("search", pattern)
            .get(), EverythingSearchResults.class);
    }
    
    @Data
    @Accessors(chain = true)
    public static class EverythingSearchResults {
        private long totalResults;
        private List<EverythingSearchResult> results;
    }
    
    @Data
    @Accessors(chain = true)
    public static class EverythingSearchResult {
        private String name;
        private String path;
        private String type;
    }
    
    private static void pressEnterKeyToContinue() {
        System.out.println("Press Enter key to continue...");
        try {
            System.in.read();
        } catch (Exception ignored) {
        }
    }
    
    private static Object[][] doConvert(List<Path> videos) {
        System.out.printf("Total of %s videos were found%n", videos.size());
        return asStream(videos)
            .map(Converter::convertVideo)
            .filter(StreamExtend::isNotEmpty)
            .map(List::toArray)
            .collect(toList())
            .toArray(Object[][]::new);
    }
    
    private static List<Path> searchVideos(String[] args) throws IOException {
        if (isEmpty(args)) {
            System.out.println("Not specify video file(s), search current folder");
            @Cleanup final Stream<Path> list = list(JAR_FOLDER);
            return flatVideos(list.map(Path::toAbsolutePath)).collect(toList());
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
