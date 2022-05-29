package com.littlearphone.dns.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.littlearphone.dns.common.CommonUtils.readBean;
import static com.littlearphone.dns.common.CommonUtils.readNode;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static org.jsoup.Jsoup.connect;

/**
 * @author yaojiamin
 * @version 1.0.0
 * @description deepin-dns-capturer
 * @date 2021/11/18 11:36
 * @since 1.0.0
 */
@Slf4j
public class ChinaZParser {
    private static final Pattern SERVERS_PATTERN = Pattern.compile("var[\\s]+servers[\\s]*=[\\s]*([^;]+);");
    private static final Map<String, String> DEFAULT_DATA = new HashMap<>() {
        {
            put("type", "1");
            put("right", "0");
            put("process", "0");
        }
    };
    
    @SneakyThrows
    public static Set<String> chinaZCollect(final Entry<String, String> entry) {
        final String domain = entry.getKey();
        final String url = "http://tool.chinaz.com/dns?type=1&host=" + domain;
        final Document document = connect(url).get();
        final String html = document.select("script[type='text/javascript']:not([src])").html();
        final Matcher serverMatcher = SERVERS_PATTERN.matcher(html);
        if (!serverMatcher.find()) {
            return emptySet();
        }
        final List<DNSNode> nodes = new ArrayList<>();
        final JsonNode servers = readNode(serverMatcher.group(1));
        try {
            for (JsonNode node : servers) {
                final JsonNode id = node.get("id");
                final String server = encode(node.get("ip").asText(), UTF_8);
                final String dns = "http://tool.chinaz.com/AjaxSeo.aspx?t=dns&server=" + server + "&id=" + id;
                final Connection connect = connect(dns);
                connect.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                connect.data(DEFAULT_DATA);
                connect.data("host", domain);
                connect.data("total", String.valueOf(servers.size()));
                final Document post = connect.post();
                final String text = post.text();
                if (!text.startsWith("(") || !text.endsWith(")")) {
                    continue;
                }
                final String json = text.substring(1, text.length() - 1);
                final List<DNSNode> list = readBean(json, DNSNodes.class).getList();
                if (isNull(list)) {
                    continue;
                }
                nodes.addAll(list);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return chinaZCollect(entry);
        }
        final Set<String> lines = nodes.stream()
            .min(comparingInt(DNSNode::getTtl))
            .map(DNSNode::logResult)
            .map(node -> node.getResult() + " " + domain)
            .map(Collections::singleton)
            .orElse(emptySet());
        if (lines.isEmpty()) {
            return chinaZCollect(entry);
        }
        if (lines.contains("0.0.0.0 " + domain)) {
            return emptySet();
        }
        return lines;
    }
    
    @Data
    @Accessors(chain = true)
    public static class DNSNodes {
        private String id;
        private String state;
        private List<DNSNode> list;
    }
    
    @Data
    @Accessors(chain = true)
    public static class DNSNode {
        private String type;
        private String result;
        @JsonProperty("ipaddress")
        private String address;
        private Integer ttl;
        
        public DNSNode logResult() {
            System.out.println("-" + getResult());
            return this;
        }
    }
}
