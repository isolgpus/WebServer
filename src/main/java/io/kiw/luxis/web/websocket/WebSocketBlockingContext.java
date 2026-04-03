package io.kiw.luxis.web.websocket;

public class WebSocketBlockingContext<IN> {
    private final IN in;

    public WebSocketBlockingContext(final IN in) {
        this.in = in;
    }

    public IN in() {
        return in;
    }


}
