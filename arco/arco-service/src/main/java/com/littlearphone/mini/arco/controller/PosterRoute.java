package com.littlearphone.mini.arco.controller;

import lombok.SneakyThrows;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.littlearphone.mini.arco.StartUp.MOVIES_PATH;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.size;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static reactor.core.publisher.Mono.empty;

@RestController
@RequestMapping("/rs")
public class PosterRoute {
    @GetMapping("/poster")
    @SneakyThrows
    public Mono<Void> handle(String uri, ServerHttpResponse response) {
        if (isBlank(uri)) {
            return empty();
        }
        if (uri.startsWith("/") || uri.startsWith("\\")) {
            uri = uri.replaceFirst("[/\\\\]", "");
        }
        final Path poster = Paths.get(MOVIES_PATH).resolve(uri);
        if (notExists(poster)) {
            return empty();
        }
        return ((ZeroCopyHttpOutputMessage) response).writeWith(poster, 0, size(poster));
    }
}