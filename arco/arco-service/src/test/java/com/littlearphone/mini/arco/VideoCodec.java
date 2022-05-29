package com.littlearphone.mini.arco;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.littlearphone.mini.arco.extend.CodecExtend.checkSuffix;
import static com.littlearphone.mini.arco.extend.CodecExtend.nameOf;
import static com.littlearphone.mini.arco.extend.CodecExtend.sizeOf;
import static com.littlearphone.mini.arco.extend.CodecExtend.toStreaming;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyBiConvert;
import static com.littlearphone.mini.arco.extend.ObjectExtend.sneakyConvert;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.notExists;
import static java.nio.file.Paths.get;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

/****************************************************************************************************************************************************
 *                                                                                                                                                  *
 *  * Copyright © 2020, Hangzhou Hikvision Digital Technology Co., Ltd. All Rights Reserved.                                                        *
 *  * This content is limited to the internal use of the Hangzhou Hikvision Digital Technology Co. and is prohibited from forwarding.               *
 *  * Website: http://www.hikvision.com                                                                                                             *
 *                                                                                                                                                  *
 ****************************************************************************************************************************************************/

@Slf4j
class VideoCodec {
    private static final Tika TIKA = new Tika();

    @Test
    void convert() {
        searchAndConvert(get("H:\\Movies"));
    }

    private void searchAndConvert(Path parent) {
        if (isNull(parent) || notExists(parent)) {
            return;
        }
        Map<Boolean, List<Path>> itemMap = sneakyConvert(parent, Files::list)
            .collect(partitioningBy(Files::isDirectory));
        itemMap.get(false).stream()
               .peek(path -> {
                   InputStream stream = sneakyConvert(path, Files::newInputStream);
                   String type = sneakyConvert(stream, TIKA::detect);
                   extensionCheck(path, type);
               })
               .filter(this::copySupportedVideo)
               .map(path -> sneakyConvert(path, Path::toRealPath))
               .forEach(path -> {
                   log.info("Find target file: {}", path);
                   final Path target = get(nameOf(path, ".mp4"));
                   if (exists(target)) {
                       log.info("File already exists, file size: {}", sizeOf(target));
                       return;
                   }
                   toStreaming(path);
               });
        itemMap.get(true).forEach(this::searchAndConvert);
    }

    private void extensionCheck(Path path, String type) {
        if (endsWithAny(type, "video/mp4", "video/quicktime") && !endsWithIgnoreCase(path.toString(), ".mp4")) {
            checkSuffix(path, ".mp4");
            return;
        }
        // TODO audio, video/x-ms-wmv, application/ogg, application/zip, image/jpeg
        if (type.startsWith("audio") || type.startsWith("application/ogg") || type.contains("application/zip")) {
            log.warn("Found a strange video file({}): {}", type, path.getFileName());
            return;
        }
        if (type.startsWith("video/x-ms-wmv") && !endsWithIgnoreCase(path.toString(), ".wmv")) {
            checkSuffix(path, ".wmv");
            return;
        }
    }

    private boolean copySupportedVideo(Path path) {
        String filePath = path.toString();
        // return contains(filePath, "ABP-298")
        //     || of(".flv", ".f4v").anyMatch(suffix -> endsWithIgnoreCase(filePath, suffix));
        return of(".flv", ".f4v", ".dat", ".mpg", ".vob").anyMatch(suffix -> endsWithIgnoreCase(filePath, suffix));
    }
}
