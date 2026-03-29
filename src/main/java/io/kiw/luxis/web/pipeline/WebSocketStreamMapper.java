package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketContext;

public interface WebSocketStreamMapper<REQ, RES, APP, RESP> {
    RES handle(WebSocketContext<REQ, APP, RESP> ctx);
}
