package io.kiw.luxis.web.pipeline;

import io.kiw.luxis.result.Result;
import io.kiw.luxis.web.http.ErrorMessageResponse;
import io.kiw.luxis.web.websocket.WebSocketBlockingAsyncContext;

import java.util.concurrent.CompletableFuture;

public interface WebSocketStreamAsyncBlockingFlatMapper<REQ, RES> {
    CompletableFuture<Result<ErrorMessageResponse, RES>> handle(WebSocketBlockingAsyncContext<REQ> ctx);
}
