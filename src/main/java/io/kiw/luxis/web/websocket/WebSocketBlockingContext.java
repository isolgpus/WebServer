package io.kiw.luxis.web.websocket;

import io.kiw.luxis.web.internal.AbstractBlockingRouteContext;

public class WebSocketBlockingContext<IN> extends AbstractBlockingRouteContext<IN> {


    public WebSocketBlockingContext(final IN in) {
        super(in);
    }
}
