package com.littlearphone.mini.arco.controller;

import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;
import static java.lang.Math.min;
import static java.util.Base64.getUrlDecoder;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@RestController
@RequestMapping("/rs")
public class MovieRoute {
    private static final int VIDEO_SEGMENT_SIZE = 10 * 1024 * 1024;

    @GetMapping("/movie/{movieId}")
    @SneakyThrows
    public Mono<Void> handle(@PathVariable String movieId, @RequestHeader("range") String ranges, ServerHttpResponse response) {
        if (isBlank(movieId)) {
            return Mono.empty();
        }
        response.setStatusCode(PARTIAL_CONTENT);
        File file = new File(new String(getUrlDecoder().decode(movieId)));
        final long total = file.length();
        long[] range = range(ranges, total);
        final HttpHeaders headers = response.getHeaders();
        // String browser = headers.getFirst("User-Agent");
        // headers.set("Etag", "W/\"9767057-1323779115364\"")
        headers.set("Content-Type", APPLICATION_OCTET_STREAM.toString());
        // headers.set("Content-Type", "video/mp4");
        // headers.set("Content-Disposition", "attachment; filename=\"" + videoName + "\"");
        headers.set("Content-Length", String.valueOf(range[1]));
        headers.set("Content-Range", "bytes " + range[0] + "-" + (range[0] + range[1] - 1) + "/" + total);
        headers.set("Accept-Ranges", "bytes");
        return ((ZeroCopyHttpOutputMessage) response).writeWith(file, range[0], range[1]);
    }

    private long[] range(String range, long total) {
        final Pattern pattern = compile("bytes[\\s=](\\d+)-(\\d+)?[/]?(\\d+)?");
        final Matcher matcher = pattern.matcher(range);
        if (!matcher.find()) {
            return new long[]{0, min(VIDEO_SEGMENT_SIZE, total)};
        }
        final String startText = matcher.group(1);
        long start = parseLong(startText);
        final String endText = matcher.group(2);
        if (isBlank(endText)) {
            return new long[]{start, min(VIDEO_SEGMENT_SIZE, total - start)};
        }
        long size = min(total - start, parseLong(endText) - start);
        return new long[]{start, min(size, VIDEO_SEGMENT_SIZE)};
    }
}
