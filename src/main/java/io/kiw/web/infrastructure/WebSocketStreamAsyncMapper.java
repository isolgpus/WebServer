package io.kiw.web.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncMapper<REQ, RES, APP> {
    CompletableFuture<RES> handle(WebSocketContext<REQ, APP> ctx);
}
