package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketBlockingContext;

public interface WebSocketStreamBlockingPeeker<REQ> {
    void handle(WebSocketBlockingContext<REQ> ctx);
}
