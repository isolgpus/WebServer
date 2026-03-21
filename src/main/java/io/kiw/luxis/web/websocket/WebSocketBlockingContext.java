package io.kiw.luxis.web.websocket;

import io.kiw.luxis.web.http.*;
import io.kiw.luxis.web.pipeline.*;
import io.kiw.luxis.web.internal.*;

public class WebSocketBlockingContext<IN> {
    private final IN in;

    public WebSocketBlockingContext(IN in) {
        this.in = in;
    }

    public IN in() {
        return in;
    }
}
