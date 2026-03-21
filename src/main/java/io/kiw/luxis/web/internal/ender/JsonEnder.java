package io.kiw.luxis.web.internal.ender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kiw.luxis.web.http.RequestContext;

public final class JsonEnder implements Ender {
    private final ObjectMapper objectMapper;

    public JsonEnder(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void end(final RequestContext requestContext, final T input) {
        try {
            requestContext.end(this.objectMapper.writeValueAsString(input));
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
