package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpContext;

public class RouteContext<IN, APP> {
    private final IN in;
    private final HttpContext http;
    private final APP app;

    public RouteContext(final IN in, final HttpContext http, final APP app) {
        this.in = in;
        this.http = http;
        this.app = app;
    }

    public IN in() {
        return in;
    }

    public HttpContext http() {
        return http;
    }

    public APP app() {
        return app;
    }
}
