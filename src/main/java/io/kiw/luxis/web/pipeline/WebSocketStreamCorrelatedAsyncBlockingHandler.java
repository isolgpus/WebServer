package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.CorrelatedWebSocketBlockingContext;

public interface WebSocketStreamCorrelatedAsyncBlockingHandler<REQ> {
    void handle(CorrelatedWebSocketBlockingContext<REQ> ctx);
}
