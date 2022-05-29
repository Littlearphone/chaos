package com.littlearphone.dns;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.littlearphone.dns.common.ChinaZParser.chinaZCollect;
import static com.littlearphone.dns.common.CommonUtils.DOMAINS;
import static com.littlearphone.dns.common.CommonUtils.judgeHostsPath;
import static com.littlearphone.dns.common.IPAddressParser.ipAddressCollect;
import static java.lang.System.getProperty;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.move;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.Files.notExists;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier;
import static javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory;
import static javax.net.ssl.SSLContext.getInstance;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

@Slf4j
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    public static void trustEveryone() {
        try {
            setDefaultHostnameVerifier((hostname, session) -> true);
            SSLContext context = getInstance("TLS");
            context.init(null, new X509TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }
                    
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }
                    
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
            }, new SecureRandom());
            setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String[] judgeFlushCmd() {
        if (startsWithIgnoreCase(getProperty("os.name"), "win")) {
            return new String[]{"ipconfig", "/flushdns"};
        }
        return new String[]{"service", "network-manager", "restart"};
    }
    
    @Bean
    public CommandLineRunner refreshDns() {
        trustEveryone();
        return args -> {
            final Path hosts = judgeHostsPath();
            if (notExists(hosts)) {
                return;
            }
            final Map<String, Set<String>> addressMap = lines(hosts)
                .filter(StringUtils::isNotBlank)
                .collect(groupingBy(this::keyMapper, LinkedHashMap::new, toSet()));
            DOMAINS.entrySet().forEach(entry -> {
                if (of(args).anyMatch(arg -> arg.contains("ipaddress"))) {
                    addressMap.put(entry.getKey(), ipAddressCollect(entry));
                } else {
                    System.out.print("Collecting address of [" + entry.getKey() + "]");
                    addressMap.put(entry.getKey(), chinaZCollect(entry));
                }
            });
            final String timestamp = now().format(ofPattern("yyyyMMddHHmmss"));
            final Path backupFolder = hosts.resolveSibling("host-bak");
            if (notExists(backupFolder)) {
                createDirectories(backupFolder);
            }
            System.out.printf("Backup path: %s", move(hosts, backupFolder.resolve("hosts-" + timestamp)));
            try (final PrintWriter writer = new PrintWriter(newBufferedWriter(hosts))) {
                addressMap.values().stream()
                    .flatMap(Collection::stream)
                    .forEach(writer::println);
            }
            final Process process = new ProcessBuilder(judgeFlushCmd())
                .redirectErrorStream(true)
                .start();
            try (final InputStream inputStream = process.getInputStream()) {
                final byte[] bytes = toByteArray(inputStream);
                System.out.println(new String(bytes, getProperty("sun.jnu.encoding")));
            }
        };
    }
    
    private String keyMapper(final String line) {
        if (line.startsWith("#")) {
            return line;
        }
        try {
            final String[] segments = line.split("[\\s\\t]+");
            return segments[segments.length - 1];
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return line;
        }
    }
}
