package io.kiw.luxis.web.http;

import io.kiw.luxis.web.internal.AbstractBlockingRouteContext;

public class BlockingRouteContext<IN> extends AbstractBlockingRouteContext<IN> {
    private final HttpContext http;


    public BlockingRouteContext(final IN in, final HttpContext http) {
        super(in);
        this.http = http;
    }

    public HttpContext http() {
        return http;
    }
}
