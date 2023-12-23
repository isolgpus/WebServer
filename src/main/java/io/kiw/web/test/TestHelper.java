package io.kiw.web.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestHelper {
    public static String json(Entry... entries)
    {

        try {
            return new ObjectMapper().writeValueAsString(object(entries).entries);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Entry entry(String key, Object value)
    {
        return new Entry(key, value);
    }

    public static JsonObject object(Entry... entries)
    {
        Map<String, Object> stringObjectHashMap = new LinkedHashMap<>();
        for (Entry entry : entries) {
            if(entry.value instanceof JsonObject)
            {
                stringObjectHashMap.put(entry.key, ((JsonObject) entry.value).entries);
            } else {
                stringObjectHashMap.put(entry.key, entry.value);
            }
        }

        return new JsonObject(stringObjectHashMap);
    }

    public static List<Object> array(Object... values) {

        return Arrays.stream(values).map(v -> v instanceof JsonObject ? ((JsonObject) v).entries : v).toList();
    }


    public static final class Entry {
        final String key;
        final Object value;

        private Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    private static class JsonObject {
        private final Map<String, Object> entries;

        public JsonObject(Map<String, Object> entries) {

            this.entries = entries;
        }
    }
}
