package com.littlearphone.fx.utils;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.littlearphone.fx.codec.AviCodec;
import com.littlearphone.fx.codec.MkvCodec;
import com.littlearphone.fx.codec.Mp4Codec;
import com.littlearphone.fx.codec.VideoCodec;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.tika.Tika;

import java.nio.file.Path;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getEncoder;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@NoArgsConstructor(access = PRIVATE)
public class IOExtend {
    public static final Tika TIKA = new Tika();
    public static final String TS_SUFFIX = ".ts";
    public static final String MP4_SUFFIX = ".mp4";
    public static final String MKV_SUFFIX = ".mkv";
    public static final String MP4_TYPE = "video/mp4";
    public static final String AVI_TYPE = "video/x-msvideo";
    public static final String MKV_TYPE = "video/x-matroska";
    public static final String TS_TYPE = "application/octet-stream";
    private static final Set<String> STREAMING_TYPES = Set.of(
        AVI_TYPE,
        MP4_TYPE
    );
    private static final Map<String, Class<? extends VideoCodec>> CODEC_MAP = Map.of(
        AVI_TYPE, AviCodec.class,
        MP4_TYPE, Mp4Codec.class,
        MKV_TYPE, MkvCodec.class
    );
    
    @SneakyThrows
    public static Class<? extends VideoCodec> detectCodec(Path path) {
        if (isNull(path)) {
            return null;
        }
        return CODEC_MAP.get(TIKA.detect(path));
    }
    
    @SneakyThrows
    public static boolean streaming(Path path) {
        if (isNull(path)) {
            return false;
        }
        final String type = TIKA.detect(path);
        if (endsWithIgnoreCase(path.toString(), TS_SUFFIX) && TS_TYPE.equalsIgnoreCase(type)) {
            return true;
        }
        return STREAMING_TYPES.contains(type);
    }
    
    public static String toBase64Text(byte[] bytes, String mimeType) {
        final Encoder encoder = getEncoder();
        final String prefix = "data:" + mimeType + ";base64,";
        return prefix + new String(encoder.encode(bytes), UTF_8);
    }
    
    public static String detectCharset(byte[] data) {
        CharsetMatch match = new CharsetDetector().setText(data).detect();
        if (match == null) {
            return UTF_8.displayName();
        }
        return match.getName();
    }
    
}
