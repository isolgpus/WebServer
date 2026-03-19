package io.kiw.web.infrastructure;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncBlockingMapper<REQ, RES> {
    CompletableFuture<RES> handle(WebSocketBlockingContext<REQ> ctx);
}
