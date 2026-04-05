package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketContext;

public interface WebSocketStreamPeeker<REQ, APP, RESP> {
    void handle(WebSocketContext<REQ, APP, RESP> ctx);
}
