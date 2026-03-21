package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketBlockingContext;

public interface WebSocketStreamBlockingMapper<REQ, RES> {
    RES handle(WebSocketBlockingContext<REQ> ctx);
}
