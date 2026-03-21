package io.kiw.web.internal.ender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kiw.web.http.VertxContext;

public final class JsonEnder implements Ender {
    private final ObjectMapper objectMapper;

    public JsonEnder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void end(VertxContext vertxContext, T input) {
        try {
            vertxContext.end(this.objectMapper.writeValueAsString(input));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
