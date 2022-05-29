package com.littlearphone.fx.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Slf4j
@RestController
@RequestMapping("/storage")
public class PageController {
    @SneakyThrows
    @GetMapping(value = "/**", produces = TEXT_HTML_VALUE)
    public String index() {
        final ClassPathResource resource = new ClassPathResource("/static/index.html");
        if (resource.exists()) {
            try (final InputStream inputStream = resource.getInputStream()) {
                return IOUtils.toString(inputStream, UTF_8);
            }
        }
        return "Index Page Not Found.";
    }
}
