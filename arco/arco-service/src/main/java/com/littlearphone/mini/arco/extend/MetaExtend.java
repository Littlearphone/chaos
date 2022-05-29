package com.littlearphone.mini.arco.extend;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.io.IOUtils;
import org.jsoup.nodes.Document;

import java.io.InputStream;
import java.util.List;

import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static lombok.AccessLevel.PRIVATE;
import static org.jsoup.Jsoup.parse;
import static org.jsoup.parser.Parser.xmlParser;

@Data
@Slf4j
@Accessors(chain = true)
@NoArgsConstructor(access = PRIVATE)
public class MetaExtend {
    private Document document;

    public static MetaExtend from(InputStream stream) {
        String text = sneakyConvert(stream, IOUtils::toString);
        return new MetaExtend().setDocument(parse(text, "", xmlParser()));
    }

    public String firstText(String selector) {
        return document.select(selector).first().text();
    }

    public List<String> text(String selector) {
        return document.select(selector).eachText();
    }
}
