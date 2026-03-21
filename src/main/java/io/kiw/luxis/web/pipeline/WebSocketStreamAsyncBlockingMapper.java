package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketBlockingContext;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncBlockingMapper<REQ, RES> {
    CompletableFuture<RES> handle(WebSocketBlockingContext<REQ> ctx);
}
