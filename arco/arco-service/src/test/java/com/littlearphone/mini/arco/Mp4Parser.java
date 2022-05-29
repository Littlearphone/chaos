package com.littlearphone.mini.arco;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;

import static com.littlearphone.mini.arco.extend.CodecExtend.readInt;
import static com.littlearphone.mini.arco.extend.CodecExtend.readString;

/****************************************************************************************************************************************************
 *                                                                                                                                                  *
 *  * Copyright © 2020, Hangzhou Hikvision Digital Technology Co., Ltd. All Rights Reserved.                                                        *
 *  * This content is limited to the internal use of the Hangzhou Hikvision Digital Technology Co. and is prohibited from forwarding.               *
 *  * Website: http://www.hikvision.com                                                                                                             *
 *                                                                                                                                                  *
 ****************************************************************************************************************************************************/

@Slf4j
public class Mp4Parser {
    // private static final String PATH = "H:\\Movies\\Ollie_Klublershturf_vs._the_Nazis_(2010)\\Ollie_Klublershturf_vs._the_Nazis_(2010)_360p_AAC.mp4";
    private static final String PATH = "H:\\20200406124532\\_Unknown\\三毛从军记_(1992)\\三毛从军记_(1992)_1080p_AAC.mp4";
    private static final String[] UNITS = new String[]{"(B)", "(KB)", "(MB)", "(GB)", "(TB)"};
    private static final byte[] _4BIT = new byte[4];

    public static void main(String[] args) throws Exception {
        try (RandomAccessFile file = new RandomAccessFile(PATH, "r")) {
            while (box(file)) {
                log.info("");
            }
        }
    }

    public static boolean box(RandomAccessFile file) throws Exception {
        final int length = readInt(file, _4BIT);
        if (length <= 0) {
            return false;
        }
        final String segment = readString(file, _4BIT);
        log.info("-------{}--------", segment);
        log.info("length={}", lengthOf(length));
        if (segment.equals("ftyp")) {
            log.info("majorBrand={}", readString(file, _4BIT));
            log.info("fversion={}", file.readInt());
            log.info("compatibleBrands={}", readString(file, new byte[length - 16]));
        } else if (segment.equals("moov")) {
            for (int i = 8; i < length; i++) {
                i += (file.skipBytes(readInfo(file) - 8) + 8);
            }
        } else {
            file.skipBytes(length - 8);
        }
        log.info("-------{}--------", segment);
        return true;
    }

    private static int readInfo(RandomAccessFile file) throws IOException {
        final int length = readInt(file, _4BIT);
        if (length <= 0) {
            return 0;
        }
        final String string = readString(file, _4BIT);
        log.info("{}, length={}", string, lengthOf(length));
        return length;
    }

    private static String lengthOf(int length) {
        int i = 0;
        // while (length > 1024) {
        //     length /= 1024;
        //     i++;
        // }
        return length + UNITS[i];
    }
}
