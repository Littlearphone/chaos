package com.littlearphone.arco.converter.utility;

import lombok.NoArgsConstructor;
import lombok.Value;

import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@NoArgsConstructor(access = PRIVATE)
public class UUIDExtend {
    public static String uuid() {
        return randomUUID().toString().replace("-", "");
    }
}
