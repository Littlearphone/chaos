package com.littlearphone.fx.utils;

import lombok.NoArgsConstructor;

import static com.littlearphone.fx.constant.Symbol.DASH;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = PRIVATE)
public class UUIDExtend {
    public static String uuid() {
        return randomUUID().toString().replace(DASH, EMPTY);
    }
}
