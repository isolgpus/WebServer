package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketContext;

public interface WebSocketStreamMapper<REQ, RES, APP> {
    RES handle(WebSocketContext<REQ, APP> ctx);
}
