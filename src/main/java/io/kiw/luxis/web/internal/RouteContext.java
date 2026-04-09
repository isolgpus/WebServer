package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpContext;

public class RouteContext<IN, APP> extends AbstractRouteContext<IN, APP> {
    private final HttpContext http;

    protected RouteContext(final IN in, final HttpContext http, final APP app) {
        super(in, app);
        this.http = http;
    }

    public HttpContext http() {
        return http;
    }
}
