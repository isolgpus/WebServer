package io.kiw.web.infrastructure;

public class WebSocketBlockingContext<IN> {
    private final IN in;
    private final WebSocketConnection connection;

    WebSocketBlockingContext(IN in, WebSocketConnection connection) {
        this.in = in;
        this.connection = connection;
    }

    public IN in() {
        return in;
    }

    public WebSocketConnection connection() {
        return connection;
    }
}
