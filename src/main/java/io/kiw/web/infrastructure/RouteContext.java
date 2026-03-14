package io.kiw.web.infrastructure;

public class RouteContext<IN, APP> {
    private final IN in;
    private final HttpContext http;
    private final APP app;

    RouteContext(IN in, HttpContext http, APP app) {
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
