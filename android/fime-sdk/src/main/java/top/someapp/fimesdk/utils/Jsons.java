/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.utils;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-04-18
 */
@SuppressWarnings("SpellCheckingInspection")
@Keep
public class Jsons {

    private static final ObjectMapper jsonReader = new JsonMapper()
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES); // 允许键名不使用引号包裹
    private static final ObjectWriter jsonWriter = new JsonMapper()
            .writer(new MinimalPrettyPrinter(""));  // 紧凑打印

    private Jsons() {
        // no instance
    }

    public static Map<String, Object> toMap(@NonNull String jsonString) {
        return toMap(new StringReader(jsonString));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(@NonNull Reader reader) {
        Map<String, Object> map = null;
        try {
            map = jsonReader.readValue(reader, Map.class);
        }
        catch (IOException ignored) {
            // ignored
        }
        return map;
    }

    public static <T> List<T> toList(@NonNull String jsonString) {
        return toList(new StringReader(jsonString));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(@NonNull Reader reader) {
        List<T> list = null;
        try {
            list = jsonReader.readValue(reader, List.class);
        }
        catch (IOException ignored) {
            // ignored
        }
        return list;
    }

    public static <T> T toBean(@NonNull String json, @NonNull Type beanType) {
        return toBean(new StringReader(json), beanType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toBean(@NonNull Reader reader, @NonNull Type beanType) {
        T obj = null;
        try {
            obj = jsonReader.readValue(reader, (Class<T>) beanType);
        }
        catch (IOException ignored) {
            // ignored
        }
        return obj;
    }

    public static String toJSONString(Map<String, Object> map) {
        return writeToJSONString(map, "{}");
    }

    public static String toJSONString(Collection<?> list) {
        return writeToJSONString(list, "[]");
    }

    public static String toJSONString(Object obj) {
        return writeToJSONString(obj, Strings.EMPTY_STRING);
    }

    public static String toJSONString(Object obj, String jsonIfNull) {
        return writeToJSONString(obj, jsonIfNull);
    }

    private static String writeToJSONString(Object obj, String valueIfNull) {
        if (obj == null) return valueIfNull;

        String json = valueIfNull;
        try {
            json = jsonWriter.writeValueAsString(obj);
        }
        catch (JsonProcessingException ignored) {
            // ignored
        }
        return json;
    }
}
