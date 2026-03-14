package io.kiw.web.infrastructure;

public class BlockingContext<IN> {
    private final IN in;
    private final HttpContext http;

    BlockingContext(IN in, HttpContext http) {
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
