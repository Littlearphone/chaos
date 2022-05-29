package com.littlearphone.dns.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.split;
import static org.jsoup.Jsoup.connect;

/**
 * @author yaojiamin
 * @version 1.0.0
 * @description deepin-dns-capturer
 * @date 2021/11/18 11:33
 * @since 1.0.0
 */
@Slf4j
public class IPAddressParser {
    public static Set<String> ipAddressCollect(final Entry<String, String> entry) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        final Set<String> result = new HashSet<>();
        System.out.println("Collecting address of [" + value + "]");
        try {
            final String detectSite = "https://websites.ipaddress.com/";
            final Document document = connect(detectSite + value).get();
            result.addAll(readAddresses(document, "//th[text()='IP Address']"));
            result.addAll(readAddresses(document, "//th[text()='IPv6 Addresses']"));
            result.addAll(readAddresses(document, "//th[text()='IPv4 Addresses']"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result.stream().map(address -> address + " " + key).collect(toSet());
    }
    
    private static Set<String> readAddresses(final Document document, final String path) {
        Elements ipv6 = document.selectXpath(path);
        if (isEmpty(ipv6)) {
            return emptySet();
        }
        final Set<String> result = new HashSet<>();
        for (Element element : ipv6) {
            final Element parent = requireNonNull(element.parent());
            final Elements addresses = parent.select("td:last-child");
            stream(split(addresses.text(), ", "))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(result::add);
        }
        return result;
    }
}
