package io.kiw.web.websocket;

import io.kiw.web.http.*;
import io.kiw.web.pipeline.*;
import io.kiw.web.internal.*;

public class WebSocketContext<IN, APP> {
    private final IN in;
    private final WebSocketConnection connection;
    private final APP app;

    public WebSocketContext(IN in, WebSocketConnection connection, APP app) {
        this.in = in;
        this.connection = connection;
        this.app = app;
    }

    public IN in() {
        return in;
    }

    public WebSocketConnection connection() {
        return connection;
    }

    public APP app() {
        return app;
    }
}
