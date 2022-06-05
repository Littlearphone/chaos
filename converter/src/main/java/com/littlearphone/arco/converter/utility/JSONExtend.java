package com.littlearphone.arco.converter.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class JSONExtend {
    private static final ObjectMapper MAPPER = createMapper();
    
    private static ObjectMapper createMapper() {
        return new ObjectMapper();
    }
    
    @SneakyThrows
    public static <T> T toBean(String json, Class<T> clazz) {
        return MAPPER.readValue(json, clazz);
    }
    
    @SneakyThrows
    public static <T> List<T> toList(String json, Class<T> clazz) {
        return MAPPER.readValue(json, MAPPER.getTypeFactory().constructParametricType(List.class, clazz));
    }
    
    @SneakyThrows
    public static JsonNode toJsonNode(String json) {
        return MAPPER.readTree(json);
    }
}
