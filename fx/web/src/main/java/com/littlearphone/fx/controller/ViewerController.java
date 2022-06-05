package com.littlearphone.fx.controller;

import com.littlearphone.fx.codec.VideoCodec;
import com.littlearphone.fx.service.StorageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.littlearphone.fx.utils.IOExtend.TIKA;
import static com.littlearphone.fx.utils.IOExtend.detectCharset;
import static com.littlearphone.fx.utils.IOExtend.detectCodec;
import static java.lang.Long.parseLong;
import static java.lang.Math.min;
import static java.net.URLDecoder.decode;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.notExists;
import static java.nio.file.Files.readAllBytes;
import static java.util.Base64.getDecoder;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@Slf4j
@RestController
@RequestMapping("/api/viewer")
public class ViewerController {
    private static final int VIDEO_SEGMENT_SIZE = 10 * 1024 * 1024;
    private static final ExecutorService SERVICE = newSingleThreadExecutor();
    private static final Map<Path, Future<?>> CONVERT_TASKS = new HashMap<>();
    @Resource
    private HttpServletRequest request;
    @Resource
    private HttpServletResponse response;
    @Resource
    private StorageService storageService;
    @Resource
    private ApplicationContext applicationContext;
    
    @SneakyThrows
    @GetMapping("/text")
    public String text(Long storageId, String location) {
        if (isBlank(location)) {
            return EMPTY;
        }
        final String decodePath = new String(getDecoder().decode(location), UTF_8);
        final Path path = storageService.resolveFile(storageId, decode(decodePath, UTF_8));
        if (notExists(path)) {
            return EMPTY;
        }
        final byte[] data = readAllBytes(path);
        return new String(data, detectCharset(data));
    }
    
    @GetMapping("/{storageId}/detect")
    @SneakyThrows
    public String detect(@PathVariable("storageId") Long storageId, String location) {
        if (isBlank(location)) {
            return "BLANK_PATH";
        }
        final String decodeString = new String(getDecoder().decode(location), UTF_8);
        final Path path = storageService.resolveFile(storageId, decodeString);
        if (notExists(path) || isDirectory(path)) {
            return "NOT_EXISTS";
        }
        final Future<?> task = CONVERT_TASKS.get(path);
        if (nonNull(task)) {
            return task.isDone() ? "CONVERTED" : "CONVERTING";
        }
        final Class<? extends VideoCodec> codecType = detectCodec(path);
        if (isNull(codecType)) {
            return "NOT_SUPPORT";
        }
        final VideoCodec videoCodec = getBean(codecType);
        if (isNull(videoCodec) || !videoCodec.testStreaming(path)) {
            return "NOT_SUPPORT";
        }
        if (videoCodec.testRestructure(path)) {
            CONVERT_TASKS.computeIfAbsent(path, key -> SERVICE.submit(() -> videoCodec.restructure(path)));
            return "CONVERTING";
        }
        return "CONVERTED";
    }
    
    @GetMapping("/stream")
    @SneakyThrows
    public void stream(Long storageId, String location,
        @RequestHeader(value = "range", required = false) String ranges) {
        if (isBlank(location)) {
            return;
        }
        final String decodeString = new String(getDecoder().decode(location), UTF_8);
        final Path path = storageService.resolveFile(storageId, decodeString);
        if (notExists(path) || isDirectory(path)) {
            return;
        }
        final Class<? extends VideoCodec> codecType = detectCodec(path);
        if (isNull(codecType)) {
            return;
        }
        final VideoCodec videoCodec = getBean(codecType);
        if (isNull(videoCodec) || !videoCodec.testStreaming(path)) {
            return;
        }
        if (videoCodec.testRestructure(path)) {
            videoCodec.restructure(path);
        }
        if (isNotBlank(ranges)) {
            response.setStatus(PARTIAL_CONTENT.value());
        }
        final long total = path.toFile().length();
        long[] range = range(ranges, total);
        // String browser = headers.getFirst("User-Agent");
        // response.setHeader("Etag", "W/\"9767057-1323779115364\"")
        response.setHeader("Content-Type", APPLICATION_OCTET_STREAM.toString());
        // response.setHeader("Content-Type", "video/mp4");
        if (isNotBlank(request.getParameter("download"))) {
            response.setHeader("Content-Transfer-Encoding", "binary");
            final String fileName = new String(path.getFileName().toString().getBytes(UTF_8), ISO_8859_1);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        }
        response.setHeader("Content-Length", String.valueOf(range[1]));
        response.setHeader("Content-Range", "bytes " + range[0] + "-" + (range[0] + range[1] - 1) + "/" + total);
        response.setHeader("Accept-Ranges", "bytes");
        try (
            final InputStream is = newInputStream(path);
            final OutputStream os = response.getOutputStream()
        ) {
            copyLarge(is, os, range[0], range[1]);
        }
    }
    
    private VideoCodec getBean(final Class<? extends VideoCodec> codecType) {
        try {
            return applicationContext.getBean(codecType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    private long[] range(String range, long total) {
        if (isBlank(range)) {
            if (isNotBlank(request.getParameter("download"))) {
                return new long[]{0, total};
            }
            return new long[]{0, min(VIDEO_SEGMENT_SIZE, total)};
        }
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
    
    @SneakyThrows
    @GetMapping("/file")
    public void file(Long storageId, String location) {
        if (isBlank(location)) {
            return;
        }
        final String decodeString = new String(getDecoder().decode(location), UTF_8);
        final Path path = storageService.resolveFile(storageId, decode(decodeString, UTF_8));
        if (notExists(path)) {
            return;
        }
        try {
            response.setContentType(TIKA.detect(path));
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            return;
        }
        try (final ServletOutputStream os = response.getOutputStream()) {
            copy(newInputStream(path), os);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }
}
