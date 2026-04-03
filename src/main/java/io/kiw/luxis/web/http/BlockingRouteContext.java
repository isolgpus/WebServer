package io.kiw.luxis.web.http;

public class BlockingRouteContext<IN> {
    private final IN in;
    private final HttpContext http;

    public BlockingRouteContext(final IN in, final HttpContext http) {
        this.in = in;
        this.http = http;
    }

    public IN in() {
        return in;
    }

    public HttpContext http() {
        return http;
    }

}
