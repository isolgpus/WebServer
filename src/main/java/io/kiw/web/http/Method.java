package io.kiw.web.http;

import io.kiw.web.jwt.*;
import io.kiw.web.validation.*;

import io.vertx.core.http.HttpMethod;

public enum Method {
    POST(HttpMethod.POST, true),
    PUT(HttpMethod.PUT, true),
    GET(HttpMethod.GET, false),
    DELETE(HttpMethod.DELETE, true),
    PATCH(HttpMethod.PATCH, true),
    OPTIONS(HttpMethod.OPTIONS, false);

    private final HttpMethod vertxMethod;
    private final boolean requiresBody;

    Method(HttpMethod vertxMethod, boolean requiresBody) {

        this.vertxMethod = vertxMethod;
        this.requiresBody = requiresBody;
    }

    public HttpMethod getVertxMethod() {
        return vertxMethod;
    }

    public boolean canHaveABody() {
        return requiresBody;
    }
}
