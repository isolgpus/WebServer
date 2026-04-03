package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.http.client.LuxisAsync;
import io.kiw.luxis.web.websocket.WebSocketBlockingAsyncContext;

public interface WebSocketStreamAsyncBlockingMapper<REQ, RES> {
    LuxisAsync<RES> handle(WebSocketBlockingAsyncContext<REQ> ctx);
}
