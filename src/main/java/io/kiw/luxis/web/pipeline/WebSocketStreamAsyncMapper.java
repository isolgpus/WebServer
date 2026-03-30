package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.websocket.WebSocketContext;

public interface WebSocketStreamAsyncMapper<REQ, RES, APP, RESP> {
    LuxisAsync<RES> handle(WebSocketContext<REQ, APP, RESP> ctx);
}
