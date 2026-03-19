package io.kiw.web.infrastructure;

public class WebSocketBlockingContext<IN> {
    private final IN in;

    WebSocketBlockingContext(IN in) {
        this.in = in;
    }

    public IN in() {
        return in;
    }
}
