package com.littlearphone.mini.arco;

import com.littlearphone.mini.arco.extend.MetaExtend;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.littlearphone.mini.arco.extend.MetaExtend.from;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Connection.Method.POST;
import static org.jsoup.Jsoup.connect;
import static org.jsoup.Jsoup.parse;

@Slf4j
class StartUpTest {

    @Test
    void testParse() {
        MetaExtend nfo = from(getClass().getClassLoader().getResourceAsStream("movie.nfo"));
        log.info(nfo.firstText("title"));
        log.info(nfo.firstText("year"));
        log.info(nfo.firstText("country"));
    }

    @Test
    void testLogin() {
        Connection connection = connect("https://accounts.douban.com/login")
                .header("Accept", "application/json")
                .header("Pragma", "no-cache")
                .header("X-Requested-With", "XMLHttpRequest")
                .data("name", "18758041013")
                .data("password", "Douban84307827")
                .data("remember", "false")
                .method(POST);
        Response response = sneakyConvert(connection, Connection::execute);
        Map<String, String> cookies = response.cookies();
        log.info("{}", cookies);
    }

    @Test
    void testSearch() {
        Connection connection = connect("https://movie.douban.com/j/subject_suggest?q=dele")
                .header("Accept", "application/json;charset=utf8")
                .header("Pragma", "no-cache")
                .header("X-Requested-With", "XMLHttpRequest")
                .ignoreContentType(true)
                .method(GET);
        Response response = sneakyConvert(connection, Connection::execute);
        log.info("{}", response.body());
    }
}