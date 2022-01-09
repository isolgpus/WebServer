package io.kiw.template.web.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestHelper {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);;

    public static String json(Entry... entries)
    {
        HashMap<String, Object> stringObjectHashMap = new LinkedHashMap<>();
        for (Entry entry : entries) {
            stringObjectHashMap.put(entry.key, entry.value);
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(stringObjectHashMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object>  jsonObject(Entry... entries)
    {
        Map<String, Object> stringObjectHashMap = new LinkedHashMap<>();
        for (Entry entry : entries) {
            stringObjectHashMap.put(entry.key, entry.value);
        }

        return stringObjectHashMap;
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
