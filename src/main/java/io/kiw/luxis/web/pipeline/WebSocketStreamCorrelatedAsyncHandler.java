package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.CorrelatedWebSocketContext;

public interface WebSocketStreamCorrelatedAsyncHandler<REQ, APP> {
    void handle(CorrelatedWebSocketContext<REQ, APP> ctx);
}
