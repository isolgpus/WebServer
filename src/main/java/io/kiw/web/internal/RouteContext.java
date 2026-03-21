package io.kiw.web.internal;

import io.kiw.web.pipeline.*;
import io.kiw.web.handler.*;
import io.kiw.web.http.*;
import io.kiw.web.validation.*;
import io.kiw.web.websocket.*;

public class RouteContext<IN, APP> {
    private final IN in;
    private final HttpContext http;
    private final APP app;

    public RouteContext(IN in, HttpContext http, APP app) {
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
