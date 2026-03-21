package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.web.websocket.WebSocketContext;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncMapper<REQ, RES, APP> {
    CompletableFuture<RES> handle(WebSocketContext<REQ, APP> ctx);
}
