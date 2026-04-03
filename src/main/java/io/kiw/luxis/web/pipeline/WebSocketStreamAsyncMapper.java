package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.websocket.WebSocketAsyncContext;

public interface WebSocketStreamAsyncMapper<REQ, RES, APP, RESP> {
    LuxisAsync<RES> handle(WebSocketAsyncContext<REQ, APP, RESP> ctx);
}
