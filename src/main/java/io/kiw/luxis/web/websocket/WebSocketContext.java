package io.kiw.luxis.web.websocket;

public class WebSocketContext<IN, APP, RESP> {
    private final IN in;
    private final WebSocketSession<RESP> connection;
    private final APP app;

    public WebSocketContext(final IN in, final WebSocketSession<RESP> connection, final APP app) {
        this.in = in;
        this.connection = connection;
        this.app = app;
    }

    public IN in() {
        return in;
    }

    public WebSocketSession<RESP> connection() {
        return connection;
    }

    public APP app() {
        return app;
    }


}
