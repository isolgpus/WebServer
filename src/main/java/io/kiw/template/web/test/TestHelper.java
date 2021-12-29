package io.kiw.template.web.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TestHelper {
    public static String json(Entry... entries)
    {
        HashMap<String, Object> stringObjectHashMap = new LinkedHashMap<>();
        for (Entry entry : entries) {
            stringObjectHashMap.put(entry.key, entry.value);
        }

        try {
            return new ObjectMapper().writeValueAsString(stringObjectHashMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Entry entry(String key, Object value)
    {
        return new Entry(key, value);
    }


    public static final class Entry {
        final String key;
        final Object value;

        private Entry(String key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
}
