package io.kiw.luxis.web.websocket;

import io.kiw.luxis.web.internal.AbstractRouteContext;

public class WebSocketRouteContext<IN, APP, RESP> extends AbstractRouteContext<IN, APP> {
    private final WebSocketSession<RESP> connection;

    public WebSocketRouteContext(final IN in, final WebSocketSession<RESP> connection, final APP app) {
        super(in, app);
        this.connection = connection;
    }

    public WebSocketSession<RESP> connection() {
        return connection;
    }
}
