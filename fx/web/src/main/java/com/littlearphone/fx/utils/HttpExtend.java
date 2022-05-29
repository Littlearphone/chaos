package com.littlearphone.fx.utils;

import lombok.SneakyThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.littlearphone.fx.constant.Symbol.AND;
import static com.littlearphone.fx.constant.Symbol.EQUAL;
import static com.littlearphone.fx.constant.Symbol.QUESTION_MARK;
import static com.littlearphone.fx.utils.StreamExtend.asStream;
import static java.net.URLEncoder.encode;
import static java.net.http.HttpClient.newHttpClient;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class HttpExtend {
    private static final HttpClient CLIENT = newHttpClient();
    private final StringBuilder url = new StringBuilder();
    private final Map<String, String> paths = new HashMap<>();
    private final Map<String, List<String>> queries = new HashMap<>();
    private final Map<String, List<String>> headers = new HashMap<>();
    
    private HttpExtend(final String url) {
        this.url.append(url);
    }
    
    public static HttpExtend url(String url) {
        return new HttpExtend(url);
    }
    
    public HttpExtend withQuery(String name, Object value) {
        queries.computeIfAbsent(name, key -> new ArrayList<>()).add(String.valueOf(value));
        return this;
    }
    
    public HttpExtend setHeader(String name, String value) {
        headers.computeIfAbsent(name, key -> new ArrayList<>()).add(value);
        return this;
    }
    
    public HttpExtend expandPath(String name, Object value) {
        paths.put(name, String.valueOf(value));
        return this;
    }
    
    @SneakyThrows
    public String get() {
        final HttpRequest request = createBuilder().GET().build();
        final HttpResponse<String> response = CLIENT.send(request, ofString(UTF_8));
        return response.body();
    }
    
    @SneakyThrows
    public <T> T get(BodyHandler<T> subscriber) {
        final HttpRequest request = createBuilder().GET().build();
        return CLIENT.send(request, subscriber).body();
    }
    
    private Builder createBuilder() throws URISyntaxException {
        final String queryString = asStream(this.queries).map(entry -> {
            final String name = entry.getKey();
            return asStream(entry.getValue())
                .map(value -> name + EQUAL + encode(value, UTF_8))
                .collect(joining(AND));
        }).collect(joining(AND));
        if (this.url.indexOf(QUESTION_MARK) >= 0) {
            this.url.append(AND).append(queryString);
        } else {
            this.url.append(QUESTION_MARK).append(queryString);
        }
        final Builder builder = HttpRequest.newBuilder().uri(new URI(this.url.toString()));
        asStream(this.headers).forEach(entry -> {
            final String name = entry.getKey();
            asStream(entry.getValue()).forEach(value -> {
                builder.header(name, value);
            });
        });
        return builder;
    }
}
