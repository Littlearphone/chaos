package com.littlearphone.fx.utils;

import org.junit.jupiter.api.Test;

import static com.littlearphone.fx.utils.HttpExtend.url;

class HttpExtendTest {
    @Test
    void testGet() {
        System.out.println(url("http://NUC:23333/")
            .withQuery("search", "xxx")
            .withQuery("json", "1")
            .get());
    }
}
