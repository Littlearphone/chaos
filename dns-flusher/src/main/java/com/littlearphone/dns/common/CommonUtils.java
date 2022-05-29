package com.littlearphone.dns.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.nio.file.Paths.get;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

/**
 * @author yaojiamin
 * @version 1.0.0
 * @description deepin-dns-capturer
 * @date 2021/11/18 11:23
 * @since 1.0.0
 */
public class CommonUtils {
    public static final Map<String, String> DOMAINS = new LinkedHashMap<>() {
        {
            put("www.apache.org", "www.apache.org");
            put("www.gravatar.com", "www.gravatar.com");
            put("gravatar.com", "gravatar.com");
            put("cdn.sstatic.net", "sstatic.net");
            put("tags.tiqcdn.com", "tags.tiqcdn.com");
            put("www.vmware.com", "www.vmware.com");
            put("vmware.com", "vmware.com");
            put("marketo.com", "marketo.com");
            put("cdnjs.cloudflare.com", "cdnjs.cloudflare.com");
            put("activemq.apache.org", "activemq.apache.org");
            put("use.fontawesome.com", "use.fontawesome.com");
            put("github.com", "github.com");
            put("api.github.com", "api.github.com");
            put("raw.github.com", "raw.github.com");
            put("help.github.com", "help.github.com");
            put("githubstatus.com", "githubstatus.com");
            put("codeload.github.com", "codeload.github.com");
            put("nodeload.github.com", "nodeload.github.com");
            put("training.github.com", "training.github.com");
            put("assets-cdn.github.com", "assets-cdn.github.com");
            put("github.githubassets.com", "github.githubassets.com");
            put("documentcloud.github.com", "documentcloud.github.com");
            // put("raw.githubusercontent.com", "raw.githubusercontent.com");
            put("gist.githubusercontent.com", "gist.githubusercontent.com");
            put("cloud.githubusercontent.com", "cloud.githubusercontent.com");
            put("github.global.ssl.fastly.net", "github.global.ssl.fastly.net");
            put("desktop.githubusercontent.com", "desktop.githubusercontent.com");
            put("avatars.githubusercontent.com", "avatars.githubusercontent.com");
            put("avatars0.githubusercontent.com", "avatars0.githubusercontent.com");
            put("avatars1.githubusercontent.com", "avatars1.githubusercontent.com");
            put("avatars2.githubusercontent.com", "avatars2.githubusercontent.com");
            put("avatars3.githubusercontent.com", "avatars3.githubusercontent.com");
            put("avatars4.githubusercontent.com", "avatars4.githubusercontent.com");
            put("avatars5.githubusercontent.com", "avatars5.githubusercontent.com");
            put("avatars6.githubusercontent.com", "avatars6.githubusercontent.com");
            put("avatars7.githubusercontent.com", "avatars7.githubusercontent.com");
            put("avatars8.githubusercontent.com", "avatars8.githubusercontent.com");
            put("user-images.githubusercontent.com", "user-images.githubusercontent.com");
            put("pkg-containers.githubusercontent.com", "pkg-containers.githubusercontent.com");
            put("repository-images.githubusercontent.com", "repository-images.githubusercontent.com");
            put("marketplace-screenshots.githubusercontent.com", "marketplace-screenshots.githubusercontent.com");
        }
    };
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .disable(FAIL_ON_EMPTY_BEANS)
        .disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature())
        .enable(ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(ALLOW_SINGLE_QUOTES);
    
    public static Path judgeHostsPath() {
        if (startsWithIgnoreCase(getProperty("os.name"), "win")) {
            return get(getenv("windir")).resolve("System32/drivers/etc/hosts");
        }
        return get("/etc/hosts");
    }
    
    @SneakyThrows
    public static JsonNode readNode(String text) {
        return MAPPER.readTree(text);
    }
    
    @SneakyThrows
    public static <T> T readBean(String text, Class<T> clazz) {
        return MAPPER.readValue(text, clazz);
    }
    
    @SneakyThrows
    public static <T> List<T> readList(String text, Class<T> clazz) {
        return MAPPER.readValue(text, MAPPER.getTypeFactory().constructParametricType(List.class, clazz));
    }
}
