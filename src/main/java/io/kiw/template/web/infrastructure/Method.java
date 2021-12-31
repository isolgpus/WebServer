package io.kiw.template.web.infrastructure;

import io.vertx.core.http.HttpMethod;

public enum Method {
    POST(HttpMethod.POST, true),
    PUT(HttpMethod.PUT, true),
    GET(HttpMethod.GET, false),
    DELETE(HttpMethod.DELETE, true);

    private final HttpMethod vertxMethod;
    private final boolean requiresBody;

    Method(HttpMethod vertxMethod, boolean requiresBody) {

        this.vertxMethod = vertxMethod;
        this.requiresBody = requiresBody;
    }

    HttpMethod getVertxMethod() {
        return vertxMethod;
    }

    public boolean canHaveABody() {
        return requiresBody;
    }
}
