package io.kiw.luxis.web.internal.ender;

import io.kiw.luxis.web.http.RequestContext;
import tools.jackson.databind.ObjectMapper;

public final class JsonEnder implements Ender {
    private final ObjectMapper objectMapper;

    public JsonEnder(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> void end(final RequestContext requestContext, final T input) {
        requestContext.end(this.objectMapper.writeValueAsString(input));
    }
}
