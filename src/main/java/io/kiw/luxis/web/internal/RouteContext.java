package io.kiw.luxis.web.internal;

import io.kiw.luxis.web.http.HttpSession;

public class RouteContext<IN, APP> extends AbstractRouteContext<IN, APP> {
    private final HttpSession http;

    protected RouteContext(final IN in, final HttpSession http, final APP app) {
        super(in, app);
        this.http = http;
    }

    public HttpSession http() {
        return http;
    }
}
