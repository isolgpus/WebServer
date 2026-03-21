package io.kiw.luxis.web.websocket;

public class WebSocketContext<IN, APP> {
    private final IN in;
    private final WebSocketConnection connection;
    private final APP app;

    public WebSocketContext(final IN in, final WebSocketConnection connection, final APP app) {
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
