package com.littlearphone.mini.arco.extend;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class UuidExtend {
    public static String uuid() {
        return randomUUID().toString().replace("-", "");
    }
}
