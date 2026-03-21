package io.kiw.web.http;

import io.kiw.web.jwt.*;
import io.kiw.web.validation.*;

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
