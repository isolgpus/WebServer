package io.kiw.web.infrastructure;

import io.kiw.result.Result;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncFlatMapper<REQ, RES, APP> {
    CompletableFuture<Result<ErrorMessageResponse, RES>> handle(WebSocketContext<REQ, APP> ctx);
}
