package io.kiw.luxis.web.http;

import io.kiw.luxis.web.internal.AbstractBlockingRouteContext;

public class BlockingRouteContext<IN> extends AbstractBlockingRouteContext<IN> {
    private final HttpSession http;


    public BlockingRouteContext(final IN in, final HttpSession http) {
        super(in);
        this.http = http;
    }

    public HttpSession http() {
        return http;
    }
}
