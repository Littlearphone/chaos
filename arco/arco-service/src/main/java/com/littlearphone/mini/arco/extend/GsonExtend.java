package com.littlearphone.mini.arco.extend;

import com.google.gson.Gson;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class GsonExtend {
    private static final Gson GSON = new Gson();

    public static String asString(Object object) {
        return GSON.toJson(object);
    }
}
