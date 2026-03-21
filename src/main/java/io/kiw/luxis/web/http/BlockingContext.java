package io.kiw.luxis.web.http;

import io.kiw.luxis.web.jwt.*;
import io.kiw.luxis.web.validation.*;

public class BlockingContext<IN> {
    private final IN in;
    private final HttpContext http;

    public BlockingContext(IN in, HttpContext http) {
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
