package io.kiw.web.websocket;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.internal.*;

public class WebSocketBlockingContext<IN> {
    private final IN in;

    public WebSocketBlockingContext(IN in) {
        this.in = in;
    }

    public IN in() {
        return in;
    }
}
