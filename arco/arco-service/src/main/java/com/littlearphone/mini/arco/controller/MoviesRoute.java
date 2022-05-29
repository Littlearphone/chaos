package com.littlearphone.mini.arco.controller;

import com.littlearphone.mini.arco.extend.CodecExtend;
import com.littlearphone.mini.arco.extend.MetaExtend;
import com.littlearphone.mini.arco.extend.TypeExtend;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64.Encoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static com.littlearphone.mini.arco.StartUp.MOVIES_PATH;
import static com.littlearphone.mini.arco.extend.GsonExtend.asString;
import static com.littlearphone.mini.arco.extend.MetaExtend.from;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static com.littlearphone.mini.arco.extend.StreamExtend.asStream;
import static java.nio.file.Files.copy;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Base64.getEncoder;
import static java.util.Base64.getUrlDecoder;
import static java.util.Base64.getUrlEncoder;
import static java.util.Objects.isNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

@Slf4j
@RestController
@RequestMapping("/rs")
public class MoviesRoute {
    private static final List<Movie> MOVIES = new ArrayList<>();
    private static final ExecutorService EXECUTOR = newFixedThreadPool(4);

    @GetMapping("/movies")
    public String handle() {
        if (isNotEmpty(MOVIES)) {
            return asString(MOVIES);
        }
        try {
            return asString(fileSearch(get(MOVIES_PATH)));
        } finally {
            MOVIES.forEach(movie -> {
                EXECUTOR.submit(() -> grabPoster(movie));
                EXECUTOR.submit(() -> analysisMedia(movie));
            });
        }
    }

    @SneakyThrows
    private void analysisMedia(Movie movie) {
        asStream(movie.getHash())
            .map(getUrlDecoder()::decode)
            .map(String::new)
            .map(Paths::get)
            .forEach(CodecExtend::toStreaming);
    }

    private void grabPoster(Movie movie) {
        final List<String> posters = movie.getPosters();
        if (isEmpty(posters)) {
            log.warn("Not found poster: {}", movie.getPath());
            return;
        }
        final String poster = posters.get(0);
        if (!startsWithIgnoreCase(poster, "http")) {
            return;
        }
        log.info("Grab movie [{}] image: {}", movie.getTitle(), poster);
        try (InputStream inputStream = createConnection(poster).getInputStream()) {
            final Path target = movie.getPath().resolve("poster.jpg");
            copy(inputStream, target, REPLACE_EXISTING);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private URLConnection createConnection(String path) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
        sslContext.init(null, new TrustManager[]{new MyX509TrustManager()}, new SecureRandom());
        final HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(path).openConnection();
        urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
        return urlConnection;
    }

    private List<Movie> fileSearch(Path moviePath) {
        Map<Boolean, List<Path>> partition = sneakyConvert(moviePath, Files::list)
            .collect(partitioningBy(Files::isDirectory));
        List<Path> videos = partition.get(false)
                                     .stream()
                                     .filter(TypeExtend::videoDetect)
                                     .collect(toList());
        List<Movie> movies = partition.get(true)
                                      .stream()
                                      .map(this::fileSearch)
                                      .flatMap(Collection::stream)
                                      .collect(toList());
        if (isEmpty(videos)) {
            return movies;
        }
        String meta = partition.get(false).stream()
                               .map(Objects::toString)
                               .filter(TypeExtend::metaDetect)
                               .findAny()
                               .orElse(null);
        if (isNull(meta)) {
            movies.addAll(videos.stream().map(Movie::new).collect(toList()));
            return movies;
        }
        final Movie movie = new Movie(moviePath, meta);
        movies.add(movie);
        MOVIES.add(movie);
        return movies;
    }

    @Data
    @Accessors(chain = true)
    public static class Movie {
        private transient Path path;
        private String title;
        private String year;
        private String meta;
        private List<String> posters;
        private List<String> country;
        private Collection<String> hash;

        public Movie(Path path) {
            setPath(sneakyConvert(path, Path::toRealPath));
            setTitle(path.getFileName().toString());
            setHash(computeHash());
        }

        private Collection<String> computeHash() {
            final Encoder encoder = getUrlEncoder();
            return sneakyConvert(getPath(), Files::list)
                .filter(TypeExtend::videoDetect)
                .map(Path::toString)
                .map(String::getBytes)
                .map(encoder::encode)
                .map(String::new)
                .collect(toList());
        }

        public Movie(Path path, String meta) {
            this(path);
            MetaExtend extend = from(sneakyConvert(path.resolve(meta), Files::newInputStream));
            this.setPosters(new ArrayList<>())
                .setTitle(extend.firstText("title"))
                .setYear(extend.firstText("year"))
                .toCountry(extend.firstText("country"))
                .addPosters(searchPoster(path))
                .addPosters(extend.text("thumb[aspect=\"poster\"]"));
        }

        private Collection<String> searchPoster(Path parent) {
            Map<String, List<Path>> paths = sneakyConvert(parent, Files::list)
                .filter(path -> startsWithIgnoreCase(path.getFileName().toString(), "poster."))
                .collect(groupingBy(CodecExtend::typeOf));
            return paths.entrySet().stream()
                        .flatMap(entry -> {
                            final List<Path> value = entry.getValue();
                            // return value.stream()
                            //             .map(path -> sneakyConvert(path, Files::newInputStream))
                            //             .map(stream -> sneakyConvert(stream, IOUtils::toByteArray))
                            //             .map(bytes -> type + getEncoder().encodeToString(bytes));
                            return asStream(value).map(MoviesRoute::toRelative);
                        })
                        .collect(toList());
        }

        public Movie addPosters(Collection<String> urls) {
            getPosters().addAll(urls);
            return this;
        }

        public Movie toCountry(String text) {
            setCountry(Stream.of(split(text, ",")).map(StringUtils::trim).collect(toList()));
            return this;
        }
    }

    private static String toRelative(Path path) {
        return path.toString().replace(Paths.get(MOVIES_PATH).toString(), "");
    }

    public static class MyX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
