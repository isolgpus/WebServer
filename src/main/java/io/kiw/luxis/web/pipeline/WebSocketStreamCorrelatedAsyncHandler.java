package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.CorrelatedWebSocketContext;

public interface WebSocketStreamCorrelatedAsyncHandler<REQ, APP, RESP> {
    void handle(CorrelatedWebSocketContext<REQ, APP, RESP> ctx);
}
