package com.littlearphone.fx.service;

import com.littlearphone.fx.entity.BaseStorage;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.littlearphone.fx.constant.Keyword.FILE;
import static com.littlearphone.fx.constant.Symbol.DOT;
import static com.littlearphone.fx.utils.HttpExtend.url;
import static com.littlearphone.fx.utils.JSONExtend.toBean;
import static com.littlearphone.fx.utils.StreamExtend.asStream;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Paths.get;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Data
@Slf4j
@Component
public class EverythingService {
    private static final int ENABLE_FEATURE = 1;
    private final AtomicInteger status = new AtomicInteger();
    @Value("${fx.everything.host}")
    private String esHost;
    
    public boolean supportEverything() {
        return isNotBlank(esHost);
    }
    
    @SneakyThrows
    public synchronized EverythingSearchResults search(final BaseStorage storage, final String path) {
        final Path parent = get(storage.getPath());
        final EverythingSearchResults results = search("\"" + parent + "\" " + path);
        asStream(results.getResults()).forEach(result -> {
            final String name = result.getName();
            final Path realPath = Path.of(result.getPath(), name);
            result.setPath(parent.relativize(realPath).toString());
            if (isDirectory(realPath)) {
                return;
            }
            final int index = name.lastIndexOf(DOT);
            if (index < 0) {
                result.setType(FILE);
                return;
            }
            result.setType(name.substring(index + 1));
        });
        return results;
    }
    
    private EverythingSearchResults search(final String pattern) {
        return toBean(url("http://" + esHost)
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
}
