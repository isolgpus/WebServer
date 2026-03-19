package io.kiw.web.infrastructure;

import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncBlockingFlatMapper<REQ, RES> {
    CompletableFuture<Result<ErrorMessageResponse, RES>> handle(WebSocketBlockingContext<REQ> ctx);
}
